'use client';
import Link from 'next/link';
import { useRouter, usePathname } from 'next/navigation';
import { jwtDecode } from 'jwt-decode';
import { useEffect, useState } from 'react';
import Image from "next/image";

const HeaderCustom = () => {
  const router = useRouter();
  const pathname = usePathname();
  const [userRole, setUserRole] = useState(null);
  const [isLoading, setIsLoading] = useState(true);

  const checkAuth = () => {
    const token = localStorage.getItem('token');
    
    if (token) {
      try {
        const decoded = jwtDecode(token);

        if (decoded.exp && Date.now() >= decoded.exp * 1000) {
            console.log('Token expired');
            logout();
            return;
        }

        setUserRole(decoded.role);
      } catch (error) {
        console.error('Invalid token:', error);
        logout();
        return;
      }
    } else {
      setUserRole(null);
    }
    setIsLoading(false);
  };

  // Logout 
  const logout = () => {
    localStorage.removeItem('token');
    setUserRole(null);
    router.push('/');
  };

  useEffect(() => {
    checkAuth();

    const expirationCheckInterval = setInterval(checkAuth, 60000);

    const handleStorageChange = () => checkAuth();
    window.addEventListener('storage', handleStorageChange);

    return () => {
        clearInterval(expirationCheckInterval);
        window.removeEventListener('storage', handleStorageChange);
    };
  }, [pathname]);

  if (isLoading) return <div className="h-16 bg-white dark:bg-gray-900"></div>;

  return (
    <header className="bg-white dark:bg-gray-900 shadow-md sticky top-0 z-50">
        <div className="mx-auto w-full max-w-screen-xl p-4 py-6">
            <div className="flex justify-between items-center">
            <div className="flex items-center">
                <Image 
                src="/coffee.svg" 
                alt="logo"
                width={40}
                height={40}
                className="mr-2"
                />
                <Link href="/" className="text-2xl font-semibold whitespace-nowrap dark:text-white">
                BrewForce
                </Link>
            </div>

            <div className="ml-4 flex items-center space-x-4">
                {userRole ? (
                <>
                <div className="hidden md:flex gap-8">
                    <nav className="hidden md:flex items-center space-x-6">
                    {(userRole === 'PEMBELI') && (
                        <Link href="/orders" className="text-gray-900 dark:text-white hover:text-amber-600 transition-colors">
                        My Orders
                        </Link>
                    )}
                    {(userRole === 'KASIR') && (
                        <Link href="/orders" className="text-gray-900 dark:text-white hover:text-amber-600 transition-colors">
                        Orders
                        </Link>
                    )}
                    {(userRole === 'KASIR' || userRole === 'ADMIN') && (
                        <Link href="/menus" className="text-gray-900 dark:text-white hover:text-amber-600 transition-colors">
                        Menu List
                        </Link>
                    )}
                    {userRole === 'ADMIN' && (
                        <Link href="/cashier" className="text-gray-900 dark:text-white hover:text-amber-600 transition-colors">
                        Cashier Management
                        </Link>
                    )}
                    </nav>

                    <button
                    onClick={logout}
                    className="flex items-center justify-center border-2 border-amber-800 text-amber-800 px-6 py-3 rounded-md hover:bg-amber-800 hover:text-white transition dark:border-amber-600 dark:text-amber-600 dark:hover:bg-amber-600 dark:hover:text-white"
                    >
                    Logout
                    </button>
                    </div>
                </>
                ) : (
                <>
                    <Link
                    href="/register"
                    className="flex items-center justify-center border-2 border-amber-800 text-amber-800 px-6 py-3 rounded-md hover:bg-amber-800 hover:text-white transition dark:border-amber-600 dark:text-amber-600 dark:hover:bg-amber-600 dark:hover:text-white"
                    >
                    Register
                    </Link>
                    <Link
                    href="/login"
                    className="flex items-center justify-center bg-amber-800 text-white px-6 py-3 rounded-md hover:bg-amber-700 transition shadow-md group dark:bg-amber-600 dark:hover:bg-amber-700"
                    >
                    Login
                    </Link>
                </>
                )}
            </div>
            </div>
        </div>
    </header>
  );
};

export default HeaderCustom;