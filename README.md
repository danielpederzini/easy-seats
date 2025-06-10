[![en](https://img.shields.io/badge/lang-en-red.svg)](https://github.com/danielpederzini/easy-seats/blob/main/README.en.md)

## Visão Geral
**Easy seats** é uma plataforma de reserva de ingressos para filmes, simples de usar, com as seguintes funcionalidades:
- Fluxo de autenticação de usuário com gerenciamento de sessões seguro usando cookies com JWT.
- Listagem de filmes com filtros de busca e gênero, com uma checagem de sessões existentes para ordenar de forma inteligente.
- Listagem de sessões para o filme especificado, com checagem de assentos livres para desabilitar o botão.
- Tela de seleção de assentos com atualizações em tempo real, gerenciamento de concorrência, lógica de expiração e liberação automática ao sair da tela.
- Sistema de status de reservas, totalmente integrado com API de pagamentos da Stripe (pagamentos, expiração, reembolsos...).
- Listagem de reservas do usuário, com ordenação inteligente baseada em status, e botões para ações relacionadas.
- Criação e validação de QRCode para que reservas sejam validadas na hora de entrar na sessão.

## Tokens e Autenticação
Tokens JWT são o principal componente de segurança dessa aplicação, todos assinados e verificados com chaves públicas e privadas fortes usando o algoritmo RSA. Os tokens contém claims padrão como issuer, audience, jti e claims personalizadas como userId e role. Todas operações autenticadas requerem um token com a devida role e userId para prevenir ataques de IDOR.

Atualmente, existem 4 tipos de JWT nessa aplicação:
- accessToken: janela de expiração muito curta.
- refreshToken: janela de expiração mais longa, invalidado após uso.
- websocketToken: janela de expiração extremamente curta, usado apenas para se conectar ao websocket.
- qrCode: janela de expiração curta, claims personalizadas para validação da reserva.

Todo tráfego de access e refresh tokens é feito por meio de cookies HTTP-only, para prevenir interceptação e roubo de tokens usando JavaScript. O refreshToken sempre estará associado a um campo na entidade usuário, e apenas um pode estar presente, o valor sendo atualizado com o novo refreshToken sempre que uma requisição de refresh é feita e apagado quando uma requisição de logout é feita.

## Listagem de Filmes
A listagem de filmes usa uma query de ordenação inteligente para mostrar primeiro os filmes com sessões ativas, e então os sem, cada subgrupo ordenado alfabeticamente. Também conta com filtros opcionais por título (%search% no SQL) e gênero.

https://github.com/user-attachments/assets/c0e00fca-5752-4dca-9836-d3b4ee149845

## Listagem de Sessões
A listagem de sessões mostra as sessões para o filme em questão, sempre ordenadas em ordem de data de início crescente. É feita uma checagem para ver se há assentos disponíveis para cada sessão, checando tanto travas temporárias no cache quanto reservas persistidas no banco.

https://github.com/user-attachments/assets/b5dd9622-aba0-4a74-8e99-fe0f7b99c8fb

## Seleção de Assentos
A tela de seleção de assentos mostra uma matriz de assentos organizados por fileiras e números. O usuário tem 5 minutos para escolher até 5 assentos e ir para o pagamento (valores configuráveis) antes que a seleção expire (para garantir que ninguém está guardando assentos sem estar reservando). A concorrência de seleção é evitada por travas, gerenciadas por um cache Redis para evitar armazenamento desnecessário de dados temporários no banco. As chaves e valores armazenados se parecem assim:

- Seat:{seatId}:{sessionId} -> UserID:{userId}
- UserLocks:{userId} -> [keys]

Sempre que um usuário entra na tela de seleção de assentos, o front-end tenta gerar um websocketToken contendo o userId e o clientId, e então o passa como um Bearer token para se inscrever no tópico da sessão.

Todas mudanças nos status dos assentos são transmitidas para o tópico da sessão correta (incluindo reservas sendo expiradas/canceladas), oferecendo feedback em tempo real para todos usuários sem prejudicar a performance. Se a seleção de assentos de um usuário expirar, ou se ele sair da tela de qualquer forma, um evento de desconexão do websocket será acionado e os assentos selecionados serão liberados.

https://github.com/user-attachments/assets/b0c20155-203d-47aa-8b33-c1482f5467c4

## Reserva e Integração de Pagamentos
Reservas são entidades que ligam o usuário à uma sessão de filme e os assentos selecionados, e os junta à informações de pagamento. Reservas usam uma variedade de status para acompanhar pagamentos, reembolsos, expirações e entre outros. Todas mudanças de status de pagamento são enviadas pela Stripe por meio de um webhook nessa aplicação, e processadas de acordo. Esses são os possíveis status de uma reserva e para quais outros eles conseguem transicionar:

- AWAITING_PAYMENT -> PAYMENT_CONFIRMED, PAYMENT_RETRY, EXPIRED
- PAYMENT_RETRY -> PAYMENT_CONFIRMED, EXPIRED
- PAYMENT_CONFIRMED -> AWAITING_CANCELLATION, PAST
- EXPIRED -> AWAITING_CANCELLATION, AWAITING_DELETION
- AWAITING_CANCELLATION -> CANCELLED
- PAST -> N/A
- CANCELLED -> N/A
- AWAITING_DELETION -> Excluído do banco de dados

Quando o usuário finaliza a seleção de assentos, uma reserva é criada com status AWAITING_PAYMENT, e o usuário é redirecionado para a página de pagamento da Stripe. Se o usuário sair do pagamento, é possível voltar indo para a tela "My Bookings". O pagamento também tem uma expiração de 5 minutos (configurável), para impedir usuários de guardarem assentos pelos quais não vão pagar. Se o pagamento expirar sem ser pago, o sistema irá checar ativamente por um pagamento chamando a API da Stripe, marcando a reserva como expirada se nada for encontrado.

Quando o pagamento é confirmado, o usuário pode gerar um QRCode para entrar na sessão, ou escolher cancelar a reserva e ser reembolsado. Se por algum motivo uma reserva foi marcada como EXPIRED mas tem um pagamento ativo na Stripe, há uma operação agendada que checa isso e reembolsa o usuário se um pagamento for encontrado, ou marca a reserva para exclusão se nada for encontrado.

https://github.com/user-attachments/assets/2b091201-5c67-4d23-a571-64401619b985

## Testes de Performance
WIP

## Cobertura de Testes
WIP
