"use client";
import { useState, useEffect } from "react";
import Link from "next/link";
import Image from "next/image";
import { jwtDecode } from "jwt-decode";
import { useRouter } from "next/navigation";

export default function Home() {
  const [loaded, setLoaded] = useState(false);
  const [userRole, setUserRole] = useState(null);
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const router = useRouter();

  useEffect(() => {
    setLoaded(true);
    
    // Check if user is logged in and get their role
    const token = localStorage.getItem('token');
    if (token) {
      try {
        const decodedToken = jwtDecode(token);
        setUserRole(decodedToken.role);
        setIsLoggedIn(true);
      } catch (err) {
        console.error("Error decoding token:", err);
        setIsLoggedIn(false);
      }
    } else {
      setIsLoggedIn(false);
    }
  }, []);

  // Function to handle login button click
  const handleLoginClick = () => {
    router.push('/login');
  };

  // Function to determine what buttons to show based on user role
  const renderActionButtons = () => {
    if (!isLoggedIn) {
      return (
        <div className="flex flex-col sm:flex-row gap-4">
          <button 
            onClick={handleLoginClick} 
            className="flex items-center justify-center bg-amber-800 text-white px-6 py-3 rounded-md hover:bg-amber-700 transition shadow-md group"
          >
            <span>Sign In</span>
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 ml-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1" />
            </svg>
          </button>
          
          <Link href="/register" className="flex items-center justify-center border-2 border-amber-800 text-amber-800 px-6 py-3 rounded-md hover:bg-amber-800 hover:text-white transition">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z" />
            </svg>
            Sign Up
          </Link>
        </div>
      );
    }

    // For admin users - show menu and cashier management
    if (userRole === "ADMIN") {
      return (
        <div className="flex flex-col sm:flex-row gap-4">
          <Link href="/menus" className="flex items-center justify-center bg-amber-800 text-white px-6 py-3 rounded-md hover:bg-amber-700 transition shadow-md group">
            <span>Manage Menu</span>
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 ml-2 transform group-hover:translate-x-1 transition" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4" />
            </svg>
          </Link>
          
          <Link href="/cashier" className="flex items-center justify-center border-2 border-amber-800 text-amber-800 px-6 py-3 rounded-md hover:bg-amber-800 hover:text-white transition">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
            </svg>
            Manage Cashiers
          </Link>
        </div>
      );
    }

    // For kasir users - emphasize order management
    if (userRole === "KASIR") {
      return (
        <div className="flex flex-col sm:flex-row gap-4">
          <Link href="/orders" className="flex items-center justify-center bg-amber-800 text-white px-6 py-3 rounded-md hover:bg-amber-700 transition shadow-md group">
            <span>Manage Orders</span>
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 ml-2 transform group-hover:translate-x-1 transition" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
            </svg>
          </Link>
          
          <Link href="/menus" className="flex items-center justify-center border-2 border-amber-800 text-amber-800 px-6 py-3 rounded-md hover:bg-amber-800 hover:text-white transition">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4" />
            </svg>
            View Menu
          </Link>
        </div>
      );
    }
    
    // For customer (PEMBELI) users - emphasize ordering
    return (
      <div className="flex flex-col sm:flex-row gap-4">
        <Link href="/orders/create" className="flex items-center justify-center bg-amber-800 text-white px-6 py-3 rounded-md hover:bg-amber-700 transition shadow-md group">
          <span>View Our Coffee</span>
          <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 ml-2 transform group-hover:translate-x-1 transition" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M14 5l7 7m0 0l-7 7m7-7H3" />
          </svg>
        </Link>
        
        <Link href="/orders" className="flex items-center justify-center border-2 border-amber-800 text-amber-800 px-6 py-3 rounded-md hover:bg-amber-800 hover:text-white transition">
          <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
          </svg>
          My Orders
        </Link>
      </div>
    );
  };

  return (
    <div className="min-h-screen bg-gradient-to-b from-amber-50 to-amber-100 font-poppins">
      {/* Subtle security pattern overlay */}
      <div className="absolute inset-0 bg-repeat opacity-5 pattern-lock"></div>
      
      <main className="relative z-10 container mx-auto px-4 py-12">
        <div className="grid md:grid-cols-2 gap-8 items-center">
          <div className={`transition-all duration-700 ${loaded ? 'translate-x-0 opacity-100' : '-translate-x-10 opacity-0'}`}>
            <h1 className="text-4xl md:text-5xl font-bold text-amber-900 mb-4">
              BrewForce Attack
            </h1>
            <div className="w-16 h-1 bg-amber-600 mb-6"></div>
            <h2 className="text-xl md:text-2xl text-amber-800 mb-2">Where security meets specialty coffee</h2>
            <p className="text-amber-700 mb-8">
              Our beans are carefully <span className="font-medium">encrypted</span> with flavor and 
              <span className="font-medium"> protected</span> by our master brewers to deliver 
              an <span className="font-medium">authenticated</span> coffee experience.
            </p>
            
            {renderActionButtons()}

            <div className="flex items-center mt-8">
              <div className="flex -space-x-2">
                {[1, 2, 3].map((i) => (
                  <div key={i} className="w-8 h-8 rounded-full border-2 border-white bg-amber-100"></div>
                ))}
              </div>
              <div className="ml-4 text-amber-700 text-sm">
                <span className="font-medium">100+</span> secure transactions today
              </div>
            </div>
          </div>

          <div className={`relative transition-all duration-700 delay-300 ${loaded ? 'translate-x-0 opacity-100' : 'translate-x-10 opacity-0'}`}>
            <div className="relative">
              <div className="w-full h-96 rounded-lg overflow-hidden shadow-xl">
                <img 
                  src="https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?ixlib=rb-4.0.3&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&w=600&q=80" 
                  alt="Coffee" 
                  className="w-full h-full object-cover"
                />
              </div>
              
              <div className="absolute -bottom-5 -left-5 bg-white p-3 rounded-lg shadow-lg flex items-center">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8 text-green-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
                </svg>
                <span className="ml-2 text-sm font-bold text-gray-900">Secured Brew</span>
              </div>
              
              <div className="absolute -top-3 -right-3 bg-amber-800 text-white p-2 rounded-full shadow-lg rotate-12">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 11c0 3.517-1.009 6.799-2.753 9.571m-3.44-2.04l.054-.09A13.916 13.916 0 008 11a4 4 0 118 0c0 1.017-.07 2.019-.203 3m-2.118 6.844A21.88 21.88 0 0015.171 17m3.839 1.132c.645-2.266.99-4.659.99-7.132A8 8 0 008 4.07M3 15.364c.64-1.319 1-2.8 1-4.364 0-1.457.39-2.823 1.07-4" />
                </svg>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4 mt-8">
              <div className="bg-white p-4 rounded-lg shadow">
                <div className="flex items-center mb-2">
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-amber-800" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4M7.835 4.697a3.42 3.42 0 001.946-.806 3.42 3.42 0 014.438 0 3.42 3.42 0 001.946.806 3.42 3.42 0 013.138 3.138 3.42 3.42 0 00.806 1.946 3.42 3.42 0 010 4.438 3.42 3.42 0 00-.806 1.946 3.42 3.42 0 01-3.138 3.138 3.42 3.42 0 00-1.946.806 3.42 3.42 0 01-4.438 0 3.42 3.42 0 00-1.946-.806 3.42 3.42 0 01-3.138-3.138 3.42 3.42 0 00-.806-1.946 3.42 3.42 0 010-4.438 3.42 3.42 0 00.806-1.946 3.42 3.42 0 013.138-3.138z" />
                  </svg>
                  <span className="ml-2 font-medium text-gray-900">Certified Beans</span>
                </div>
                <p className="text-sm text-gray-800 font-medium">Premium encrypted flavors</p>
              </div>
              
              <div className="bg-white p-4 rounded-lg shadow">
                <div className="flex items-center mb-2">
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-amber-800" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                  </svg>
                  <span className="ml-2 font-medium text-gray-900">Secure Brewing</span>
                </div>
                <p className="text-sm text-gray-800 font-medium">Protected preparation methods</p>
              </div>
            </div>
          </div>
        </div>
      </main>

      <style jsx>{`
        .pattern-lock {
          background-image: url("data:image/svg+xml,%3Csvg width='20' height='20' viewBox='0 0 20 20' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M10 12a2 2 0 100-4 2 2 0 000 4zm0 2a4 4 0 110-8 4 4 0 010 8zm-2-4a2 2 0 11-4 0 2 2 0 014 0zm8 0a2 2 0 11-4 0 2 2 0 014 0z' fill='%23805500' fill-opacity='0.2' fill-rule='evenodd'/%3E%3C/svg%3E");
        }
      `}</style>
    </div>
  );
}