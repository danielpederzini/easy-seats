"use client"

import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { SeatUpdate } from '@/lib/types';
import { useEffect, useRef, useState } from 'react';
import apiClient from '@/lib/utils/api-client';
import { authForWebsocket } from '@/lib/api/auth';

function useSeatWebSocket(sessionId: number, ttl: number, onSeatUpdate: (update: SeatUpdate) => void) {
  const [status, setStatus] = useState("starting");
  const clientRef = useRef<Client | null>(null);
  const timeoutRef = useRef<NodeJS.Timeout | null>(null);

  useEffect(() => {
    let isMounted = true;

    const fetchWebSocketTokenAndConnect = async () => {
      try {
        const clientId = apiClient.getClientId();
        if (!clientId) return;

        const webSocketToken = await authForWebsocket(clientId);
        if (!webSocketToken) return;

        const client = new Client({
          webSocketFactory: () => new SockJS(`http://localhost:8080/ws/seats`),
          connectHeaders: {
            Authorization: `Bearer ${webSocketToken}`,
            movieSessionId: sessionId.toString()
          },
          reconnectDelay: 5000,
          heartbeatIncoming: 4000,
          heartbeatOutgoing: 4000,
        });

        client.onConnect = () => {
          client.subscribe(`/topic/session/${sessionId}/seats`, (message) => {
            const update: SeatUpdate = JSON.parse(message.body);
            if (update.originId !== clientId) {
              onSeatUpdate(update);
            }
          });
        };

        client.activate();
        clientRef.current = client;

        timeoutRef.current = setTimeout(() => {
          setStatus("expired");
          client.deactivate();
        }, ttl);
      } catch (error) {
        if (isMounted) {
          setStatus("connection-failed");
        }
        console.error('Failed to fetch WebSocket token or connect:', error);
      }
    };

    fetchWebSocketTokenAndConnect();

    return () => {
      isMounted = false;
      if (timeoutRef.current) clearTimeout(timeoutRef.current);
      if (clientRef.current) clientRef.current.deactivate();
    };
  }, [sessionId, onSeatUpdate, ttl]);

  return status;
}

export { useSeatWebSocket }