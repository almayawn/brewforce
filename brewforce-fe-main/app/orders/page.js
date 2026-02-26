'use client';

import React, { useState, useEffect } from "react";
import { jwtDecode } from "jwt-decode";
import OrderListPembeli from "../../components/OrderListPembeli";
import OrderListKasir from "../../components/OrderListKasir";
import { useRouter } from "next/navigation";

const OrderDetail = () => {
  const [orders, setOrders] = useState([]);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);
  const [token, setToken] = useState(null);

  const router = useRouter();

  useEffect(() => {
    const storedToken = localStorage.getItem('token');
    setToken(storedToken);

    if (!storedToken) {
      router.push('/login');
      return;
    }

    const fetchOrders = async () => {
      try {
        const decodedToken = jwtDecode(storedToken);
        const userRole = decodedToken.role;

        let apiUrl = "";
        if (userRole === "PEMBELI") {
          apiUrl = `${process.env.NEXT_PUBLIC_API_URL_BE}/api/orders/my-orders`;
        } else if (userRole === "KASIR") {
          apiUrl = `${process.env.NEXT_PUBLIC_API_URL_BE}/api/orders`;
        } else {
          throw new Error("Unauthorized access");
        }

        const res = await fetch(apiUrl, {
          headers: {
            Authorization: `Bearer ${storedToken}`,
          },
        });

        if (!res.ok) {
          throw new Error("Failed to fetch orders");
        }

        const data = await res.json();
        setOrders(data);

        if (userRole !== "PEMBELI" && userRole !== "KASIR") {
          throw new Error("Unauthorized access");
        }
        
      } catch (error) {
        setError(error.message);
        console.error("Error fetching orders:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchOrders();
  }, []);

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-b from-amber-50 to-amber-100 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin h-12 w-12 mb-4 border-4 border-amber-800 border-t-transparent rounded-full mx-auto"></div>
          <p className="text-amber-800">Loading orders...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen bg-gradient-to-b from-amber-50 to-amber-100 flex items-center justify-center">
        <div className="bg-white p-8 rounded-lg shadow-xl border border-amber-200 max-w-md w-full text-center">
          <div className="text-red-600 mb-2">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-12 w-12 mx-auto" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
          </div>
          <h2 className="text-2xl font-bold text-amber-900 mb-4">Error: </h2>
          <p className="text-amber-800">{error}</p>
        </div>
      </div>
    );
  }

  const userRole = jwtDecode(token).role;
  return userRole === "PEMBELI" ? (
    <OrderListPembeli orders={orders} />
  ) : (
    <OrderListKasir orders={orders} />
  );
};

export default OrderDetail;
