'use client';

import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import RegistrationForm from '@/components/forms/RegistrationForm';
import RegistrationCashier from '@/components/forms/RegistrationCashier';
import { jwtDecode } from 'jwt-decode';

const RegisterPage = () => {
  const router = useRouter();
  const [userRole, setUserRole] = useState(null);
  const [loading, setLoading] = useState(true);
  const [token, setToken] = useState(null);

  const logout = () => {
    localStorage.removeItem('token');
    router.push('/');
  };

  useEffect(() => {
    try {
      const storedToken = localStorage.getItem('token');
      if (storedToken) {
        setToken(storedToken); // Store token in state
        const decoded = jwtDecode(storedToken);
        setUserRole(decoded.role);
      }
    } catch (error) {
      console.error('Error checking authentication:', error);
    } finally {
      setLoading(false);
    }
  }, []);

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-b from-amber-50 to-amber-100 flex items-center justify-center">
        <div className="animate-spin h-12 w-12 border-4 border-amber-800 border-t-transparent rounded-full"></div>
      </div>
    );
  }

  if (!userRole) {
    return (
      <div className="min-h-screen bg-gradient-to-b from-amber-50 to-amber-100">
        <RegistrationForm />
      </div>
    );
  }

  if (userRole === 'ADMIN') {
    return (
      <div className="min-h-screen bg-gradient-to-b from-amber-50 to-amber-100">
        <RegistrationCashier token={token} />
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-b from-amber-50 to-amber-100 flex items-center justify-center">
      <div className="bg-white p-8 rounded-lg shadow-xl border border-amber-200 max-w-md w-full text-center">
        <div className="mb-6">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            className="h-16 w-16 text-amber-600 mx-auto"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"
            />
          </svg>
        </div>
        <h2 className="text-2xl font-bold text-amber-900 mb-4">
          Unauthorized Access
        </h2>
        <p className="text-amber-800 mb-6">
          Please log out first or login with an admin account to access this page.
        </p>
        <button
          onClick={logout}
          className="bg-amber-800 text-white px-6 py-2 rounded-md hover:bg-amber-700 transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-amber-500"
        >
          Logout
        </button>
      </div>
    </div>
  );
};

export default RegisterPage;