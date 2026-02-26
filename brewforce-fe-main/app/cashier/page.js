"use client";

import React, { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { jwtDecode } from "jwt-decode";
import CashierList from "@/components/CashierList";

export default function CashierListPage() {
  const router = useRouter();
  const [cashiers, setCashiers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [isAdmin, setIsAdmin] = useState(false);

  useEffect(() => {
    const token = localStorage.getItem('token');
    
    if (!token) {
      router.push('/login');
      return;
    }

    try {
      const decoded = jwtDecode(token);
      
      if (decoded.role !== 'ADMIN') {
        setError("Unauthorized: Only Admin can access cashier management");
        setIsAdmin(false);
        setLoading(false);
        return;
      }
      
      setIsAdmin(true);
      fetchCashiers(token);
    } catch (err) {
      console.error('Invalid token:', err);
      setError('Authentication failed');
      setLoading(false);
    }
  }, [router]);

  const fetchCashiers = async (token) => {
    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL_AUTH}/api/users/cashiers`, {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });

      if (!response.ok) {
        throw new Error('Failed to fetch cashiers');
      }

      const data = await response.json();
      setCashiers(data.data || []);
    } catch (err) {
      console.error('Error fetching cashiers:', err);
      setError(err.message || 'Failed to load cashiers');
    } finally {
      setLoading(false);
    }
  };

  if (error && error.includes("Unauthorized")) {
    return (
      <div className="min-h-screen bg-gradient-to-b from-amber-50 to-amber-100 font-poppins flex items-center justify-center">
        <div className="text-center">
          <div className="text-red-600 mb-2">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-12 w-12 mx-auto" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
          </div>
          <p className="text-xl font-bold text-amber-900">Error: {error}</p>
          <p className="text-amber-800 mt-2">
            You don't have permission to access this page.
          </p>
          <button 
            onClick={() => router.push('/')}
            className="mt-4 px-4 py-2 bg-amber-800 text-white rounded hover:bg-amber-700 transition"
          >
            Back to Home
          </button>
        </div>
      </div>
    );
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-b from-amber-50 to-amber-100 font-poppins flex items-center justify-center">
        <div className="coffee-loading">
          <div className="coffee-cup"></div>
          <div className="coffee-steam steam-1"></div>
          <div className="coffee-steam steam-2"></div>
          <div className="coffee-steam steam-3"></div>
          <p className="mt-4 text-amber-800 font-medium">Loading cashier data...</p>
        </div>
        <style jsx>{`
          .coffee-loading {
            display: flex;
            flex-direction: column;
            align-items: center;
          }
          .coffee-cup {
            width: 40px;
            height: 30px;
            border: 3px solid #78350f;
            border-radius: 0 0 15px 15px;
            position: relative;
            background: linear-gradient(to bottom, #f4f4f5 0%, #f4f4f5 30%, #92400e 30%, #92400e 100%);
          }
          .coffee-cup:after {
            content: '';
            position: absolute;
            width: 15px;
            height: 15px;
            border: 3px solid #78350f;
            border-radius: 50%;
            right: -18px;
            top: 2px;
          }
          .coffee-steam {
            width: 8px;
            height: 20px;
            background-color: #d6d6d6;
            border-radius: 10px;
            margin: 0 auto;
            position: relative;
            top: -12px;
            opacity: 0;
          }
          .steam-1 {
            animation: steaming 2s infinite 0.2s;
            margin-left: -15px;
          }
          .steam-2 {
            animation: steaming 2s infinite 0.6s;
          }
          .steam-3 {
            animation: steaming 2s infinite 1s;
            margin-left: 15px;
          }
          @keyframes steaming {
            0% {
              transform: translateY(0) scaleX(1);
              opacity: 0;
            }
            15% {
              opacity: 0.8;
            }
            50% {
              transform: translateY(-20px) scaleX(3);
              opacity: 0;
            }
            100% {
              transform: translateY(-40px) scaleX(1);
              opacity: 0;
            }
          }
        `}</style>
      </div>
    );
  }

  // Main content (only shown for admin users)
  return (
    <div className="min-h-screen bg-gradient-to-b from-amber-50 to-amber-100">
      <div className="max-w-4xl mx-auto px-4 py-8 sm:px-6 lg:px-8">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-amber-900 mb-2">Cashier Management</h1>
          <div className="w-24 h-1 bg-amber-600 mx-auto mb-4"></div>
          <p className="text-amber-800">
            Manage all cashier accounts in your system
          </p>
        </div>

        <CashierList 
          cashiers={cashiers} 
          loading={loading} 
          error={error} 
          isAdmin={isAdmin} 
        />
      </div>
    </div>
  );
}