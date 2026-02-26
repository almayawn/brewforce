'use client';
import React, { useEffect, useState } from "react";
import { useRouter } from 'next/navigation';
import { jwtDecode } from 'jwt-decode';
import LoginForm from "@/components/forms/LoginForm";

const LoginPage = () => {
  const router = useRouter();
  const [loaded, setLoaded] = useState(false);

  useEffect(() => {
    setLoaded(true);
    // Check if user is already logged in
    const storedToken = localStorage.getItem('token');
    if (storedToken) {
      router.push('/');
    }
  }, [router]);

  return (
    <div className="min-h-screen bg-gradient-to-b from-amber-50 to-amber-100 font-poppins">
      {/* Background pattern */}
      <div className="absolute inset-0 bg-repeat opacity-5 pattern-lock"></div>

      <div className="relative z-10 container mx-auto px-4 py-12">
        <div className="grid md:grid-cols-2 gap-8 items-center">
          {/* Left side - Image and Features */}
          <div className={`transition-all duration-700 ${loaded ? 'translate-x-0 opacity-100' : '-translate-x-10 opacity-0'}`}>
            <div className="relative">
              <div className="w-full h-[500px] rounded-lg overflow-hidden shadow-xl">
                <img 
                  src="https://images.unsplash.com/photo-1495474472287-4d71bcdd2085"
                  alt="Coffee Shop" 
                  className="w-full h-full object-cover"
                />
                <div className="absolute inset-0 bg-gradient-to-t from-black/50 to-transparent">
                  <div className="absolute bottom-6 left-6 text-white">
                    <h2 className="text-3xl font-bold mb-2">Welcome to BrewForce</h2>
                    <p className="text-sm opacity-90">Your favorite coffee experience</p>
                  </div>
                </div>
              </div>
            </div>

            {/* Feature cards */}
            <div className="grid grid-cols-2 gap-4 mt-8">
              <div className="bg-white p-4 rounded-lg shadow">
                <div className="flex items-center mb-2">
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-amber-800" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                  </svg>
                  <span className="ml-2 font-medium text-amber-900">Secure Login</span>
                </div>
                <p className="text-sm text-amber-700">Your data is always protected</p>
              </div>
              <div className="bg-white p-4 rounded-lg shadow">
                <div className="flex items-center mb-2">
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-amber-800" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
                  </svg>
                  <span className="ml-2 font-medium text-amber-900">Fast Access</span>
                </div>
                <p className="text-sm text-amber-700">Quickly get to your dashboard</p>
              </div>
            </div>
          </div>

          {/* Right side - Login Form */}
          <div className={`transition-all duration-700 delay-300 ${loaded ? 'translate-x-0 opacity-100' : 'translate-x-10 opacity-0'}`}>
            <LoginForm />
          </div>
        </div>
      </div>

      <style jsx>{`
        .pattern-lock {
          background-image: url("data:image/svg+xml,%3Csvg width='20' height='20' viewBox='0 0 20 20' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M10 12a2 2 0 100-4 2 2 0 000 4zm0 2a4 4 0 110-8 4 4 0 010 8zm-2-4a2 2 0 11-4 0 2 2 0 014 0zm8 0a2 2 0 11-4 0 2 2 0 014 0z' fill='%23805500' fill-opacity='0.2' fill-rule='evenodd'/%3E%3C/svg%3E");
        }
      `}</style>
    </div>
  );
};

export default LoginPage;