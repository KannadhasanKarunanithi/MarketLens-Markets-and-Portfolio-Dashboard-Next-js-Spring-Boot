import type { Metadata } from "next";
import { Space_Grotesk, JetBrains_Mono } from "next/font/google";
import "./globals.css";
import { Providers } from "@/lib/providers";

const display = Space_Grotesk({
  variable: "--font-display",
  subsets: ["latin"],
});

const monoData = JetBrains_Mono({
  variable: "--font-mono-data",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "MarketLens",
  description: "Markets and portfolio dashboard",
};

export default function RootLayout({ children }: LayoutProps<"/">) {
  return (
    <html lang="en" className={`${display.variable} ${monoData.variable} h-full`}>
      <body className="min-h-full">
        <Providers>{children}</Providers>
      </body>
    </html>
  );
}
