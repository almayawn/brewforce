"use client";

import React, { useState } from "react";
import moment from "moment";
import Link from "next/link";

const OrderListPembeli = ({ orders }) => {
  const [showModal, setShowModal] = useState(false);
  const hasActiveOrder = orders.some(order => 
    ["AWAITING_PAYMENT", "PREPARING", "READY"].includes(order.status)
  );
  const handleCreateOrderClick = (e) => {
    if (hasActiveOrder) {
      e.preventDefault();
      setShowModal(true); // Tampilkan modal alih-alih alert
    }
  };
  const Modal = () => {
    if (!showModal) return null;

    return (
      <div className="fixed inset-0 flex items-center justify-center z-50">
        <div className="absolute inset-0 bg-amber-50/80 backdrop-blur-sm"></div>
        <div className="bg-white p-8 rounded-lg shadow-xl max-w-md w-full mx-4 z-10 border border-amber-200">
          <div className="flex flex-col items-center">
            <div className="mb-4 text-amber-600">
              <svg xmlns="http://www.w3.org/2000/svg" className="h-16 w-16" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
              </svg>
            </div>
            <h3 className="text-xl font-semibold text-amber-900 mb-4">Order Aktif Ditemukan</h3>
            <p className="text-gray-600 text-center mb-6">
              Anda masih memiliki pesanan yang aktif. Mohon tunggu pesanan selesai sebelum membuat pesanan baru.
            </p>
            <button
              onClick={() => setShowModal(false)}
              className="bg-amber-600 text-white px-6 py-2 rounded-lg hover:bg-amber-700 transition-colors"
            >
              Tutup
            </button>
          </div>
        </div>
      </div>
    );
  };

  return (
    <div className="mx-12 p-4 pb-20 bg-gradient-to-b from-amber-50 to-amber-100">
        <Modal />
       {orders.length > 0 && (
      <div className="flex mt-24 justify-between items-center mb-4">
        <h1 className="text-4xl md:text-5xl font-bold text-amber-900">Riwayat</h1>
        <Link
            href={hasActiveOrder ? "#" : "/orders/create"}
            onClick={handleCreateOrderClick}
            className={`${
              hasActiveOrder 
                ? "bg-gray-400 cursor-not-allowed" 
                : "bg-amber-900 hover:bg-amber-700"
            } text-white px-4 py-2 rounded-lg text-lg font-semibold transition-colors`}
          >
            Create Order
          </Link>
      </div>  )}

      {orders.length === 0 ? (
        <div className="flex flex-col items-center justify-center mt-24">
          <div className="coffee-loading">
            <div className="coffee-cup"></div>
            <div className="coffee-steam steam-1"></div>
            <div className="coffee-steam steam-2"></div>
            <div className="coffee-steam steam-3"></div>
        </div>
          <h2 className="text-2xl font-semibold text-amber-800 mb-4">
            Belum ada pesanan
          </h2>
          <p className="text-gray-600 text-center mb-6">
            Anda belum membuat pesanan. Yuk, pesan kopi favorit Anda sekarang!
          </p>
          <Link
            href="/orders/formOrder"
            className="bg-amber-900 hover:bg-amber-700 text-white px-6 py-3 mb-20 rounded-lg text-lg font-semibold transition-colors"
          >
            Create Order
          </Link>
        </div>
      ) : (
        <div className="space-y-4 mb-24">
          {[...orders].reverse().map((order) => (
            <Link href={`/orders/${order.idOrder}`} key={order.idOrder}>
              <div className="bg-white mt-4 p-4 shadow-md rounded-lg flex flex-col space-y-2 cursor-pointer hover:bg-gray-50 transition">
                <div className="flex justify-between">
                  <span className="text-xl font-bold flex items-center gap-2 text-amber-900">
                    {moment(order.createdDateTime).locale("id").format("D MMMM YYYY HH:mm")}
                  </span>
                  <strong
                    className={`px-3 py-1 rounded-full text-m font-bold ${
                      {
                        AWAITING_PAYMENT: "bg-yellow-100 text-yellow-700",
                        PREPARING: "bg-blue-100 text-blue-700",
                        READY: "bg-green-100 text-green-700",
                        COMPLETED: "bg-teal-100 text-teal-700",
                        CANCELLED: "bg-red-100 text-red-700",
                      }[order.status] || "bg-gray-100 text-gray-700"
                    }`}
                  >
                    {order.status}
                  </strong>
                </div>
                <div className="text-sm text-gray-500"></div>
                <div className="space-y-1">
                  {order.items.map((item, index) => (
                    <div key={index} className="text-amber-700">
                      {item.quantity} x {item.menuName}
                    </div>
                  ))}
                </div>
                <div className="text-amber-900 mt-2 border-t border-gray-200 pt-4">
                  <div className="flex justify-between">
                    <p className="text-lg font-semibold">Total Harga</p>
                    <p className="text-lg font-semibold">
                      Rp {order.totalHarga.toLocaleString()}
                    </p>
                  </div>
                </div>
              </div>
            </Link>
          ))}
        </div>
      )}
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
};

export default OrderListPembeli;