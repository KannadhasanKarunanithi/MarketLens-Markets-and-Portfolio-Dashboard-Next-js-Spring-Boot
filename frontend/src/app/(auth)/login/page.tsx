"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useEffect, useState } from "react";
import { ApiError } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import { Button, ErrorNote, Field, inputClass } from "@/components/ui";

export default function LoginPage() {
  const router = useRouter();
  const { login, status } = useAuth();
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    if (status === "authenticated") router.replace("/dashboard");
  }, [status, router]);

  const submit = async (event: React.FormEvent) => {
    event.preventDefault();
    setError(null);
    setBusy(true);
    try {
      await login(username.trim(), password);
      router.replace("/dashboard");
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not sign in");
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="rounded-md border border-line bg-panel p-6">
      <h2 className="text-lg font-semibold text-ink">Sign in</h2>
      <p className="mt-1 text-sm text-ink-dim">Use your MarketLens account.</p>

      <form className="mt-5 space-y-4" onSubmit={submit}>
        <Field label="Username">
          <input
            className={inputClass}
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            autoComplete="username"
            required
          />
        </Field>
        <Field label="Password">
          <input
            className={inputClass}
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="current-password"
            required
          />
        </Field>
        {error ? <ErrorNote message={error} /> : null}
        <Button type="submit" disabled={busy} className="w-full">
          {busy ? "Signing in" : "Sign in"}
        </Button>
      </form>

      <p className="mt-4 text-sm text-ink-dim">
        New here?{" "}
        <Link href="/register" className="font-medium text-gold hover:underline">
          Create an account
        </Link>
      </p>
      <p className="tnum mt-2 text-xs text-ink-faint">
        Demo login: <span className="text-ink-dim">demo</span> / <span className="text-ink-dim">demo12345</span>
      </p>
    </div>
  );
}
