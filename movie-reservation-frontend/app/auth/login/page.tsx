"use client"

import React from "react";
import LoginForm from "@/components/auth/LoginForm";
import { useSearchParams } from "next/navigation";

export default function LoginPage() {
  const searchParams = useSearchParams();

  const redirectUrlParam = searchParams.get("redirect");

  return (
    <div className="flex min-h-[calc(100vh-4rem)] flex-col items-center justify-center py-12 px-4 sm:px-6 lg:px-8 bg-black">
      <LoginForm redirectUrl={redirectUrlParam} />
    </div>
  );
}