"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { ApiError } from "@/lib/api";
import { useAuth } from "@/lib/auth";
import { Button, ErrorNote, Field, inputClass } from "@/components/ui";

export default function RegisterPage() {
  const router = useRouter();
  const { register } = useAuth();
  const [username, setUsername] = useState("");
  const [displayName, setDisplayName] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  const submit = async (event: React.FormEvent) => {
    event.preventDefault();
    setError(null);
    setBusy(true);
    try {
      await register(username.trim(), displayName.trim(), password);
      router.replace("/dashboard");
    } catch (err) {
      setError(err instanceof ApiError ? err.message : "Could not create the account");
    } finally {
      setBusy(false);
    }
  };

  return (
    <div className="rounded-md border border-line bg-panel p-6">
      <h2 className="text-lg font-semibold text-ink">Create your account</h2>
      <p className="mt-1 text-sm text-ink-dim">It takes a moment.</p>

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
        <Field label="Display name">
          <input
            className={inputClass}
            value={displayName}
            onChange={(e) => setDisplayName(e.target.value)}
            autoComplete="name"
            required
          />
        </Field>
        <Field label="Password">
          <input
            className={inputClass}
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="new-password"
            minLength={8}
            required
          />
        </Field>
        {error ? <ErrorNote message={error} /> : null}
        <Button type="submit" disabled={busy} className="w-full">
          {busy ? "Creating" : "Create account"}
        </Button>
      </form>

      <p className="mt-4 text-sm text-ink-dim">
        Already have an account?{" "}
        <Link href="/login" className="font-medium text-gold hover:underline">
          Sign in
        </Link>
      </p>
    </div>
  );
}
