'use client';

import React, { useState, useEffect } from "react";
import { jwtDecode } from "jwt-decode";
import OrderDetailPembeli from "../../../components/OrderDetailPembeli";
import OrderDetailKasir from "../../../components/OrderDetailKasir";
import { useRouter } from "next/navigation";

const OrderDetail = ({ params }) => {
  const [order, setOrder] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(true);
  const [userRole, setUserRole] = useState(null);
  const [token, setToken] = useState(null);

  const router = useRouter();
  const orderId = React.use(params).id;
  useEffect(() => {
    const storedToken = localStorage.getItem('token');
    setToken(storedToken);

    if (!storedToken) {
      router.push('/login');
      return;
    }

    const fetchOrderDetails = async () => {
      try {
        const decodedToken = jwtDecode(storedToken);
        setUserRole(decodedToken.role);

        const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL_BE}/api/orders/${orderId}`, {
          headers: {
            Authorization: `Bearer ${storedToken}`,
          },
        });

        if (!res.ok) {
          throw new Error("Failed to fetch order details");
        }

        const data = await res.json();
        setOrder(data);
      } catch (error) {
        setError(error.message);
      } finally {
        setLoading(false);
      }
    };

    fetchOrderDetails();
  }, [orderId, router]);

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-b from-amber-50 to-amber-100 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin h-12 w-12 mb-4 border-4 border-amber-800 border-t-transparent rounded-full mx-auto"></div>
          <p className="text-amber-800">Loading order details...</p>
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

  if (userRole === "PEMBELI") {
    return <OrderDetailPembeli order={order} />;
  } else if (userRole === "KASIR") {
    return <OrderDetailKasir order={order} />;
  } else {
    return (
      <div className="min-h-screen bg-gradient-to-b from-amber-50 to-amber-100 flex items-center justify-center">
        <div className="bg-white p-8 rounded-lg shadow-xl border border-amber-200">
          <h2 className="text-2xl font-bold text-amber-900 mb-4">Unauthorized Access</h2>
          <p className="text-amber-800">You don't have permission to view this order.</p>
        </div>
      </div>
    );
  }
};

export default OrderDetail;
