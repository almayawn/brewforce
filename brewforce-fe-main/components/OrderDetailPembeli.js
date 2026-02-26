"use client";

import moment from "moment";
import React, { useState } from "react";
import { useRouter } from "next/navigation"; 
import { FaCheckCircle, FaClock, FaExclamationCircle, FaTimesCircle } from 'react-icons/fa'
const OrderDetailPembeli = ({ order: initialOrder }) => {
  const [order, setOrder] = useState(initialOrder);
  const [showModal, setShowModal] = useState(false);
  const [modalMessage, setModalMessage] = useState('');
  const [isError, setIsError] = useState(false);
  const Modal = () => {
    if (!showModal) return null;
  
    return (
      <div className="fixed inset-0 flex items-center justify-center z-50">
        <div className="absolute inset-0 bg-amber-50/80 backdrop-blur-sm"></div>
        <div className="bg-white p-8 rounded-lg shadow-xl max-w-md w-full mx-4 z-10 border border-amber-200">
          <div className="flex flex-col items-center">
            <div className="mb-4 text-amber-600">
              {isError ? (
                <svg xmlns="http://www.w3.org/2000/svg" className="h-16 w-16" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
              ) : (
                <svg xmlns="http://www.w3.org/2000/svg" className="h-16 w-16" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                </svg>
              )}
            </div>
            <h3 className="text-xl font-semibold text-amber-900 mb-4">
              {isError ? 'Gagal Membatalkan Pesanan' : 'Pesanan Dibatalkan'}
            </h3>
            <p className="text-gray-600 text-center mb-6">{modalMessage}</p>
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

  const handleCancelOrder = async (idOrder) => {
    try {
    const storedToken = localStorage.getItem("token");
  
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL_BE}/api/orders/cancel`, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${storedToken}`,
        },
        body: JSON.stringify({
          idOrder: idOrder,
          status: "CANCELLED", // Status langsung di-set ke "CANCELLED"
        }),
      });
  
      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || "Failed to cancel order");
      }
  
      const cancelledOrder = await response.json();
      console.log("Order cancelled successfully:", cancelledOrder);
      setOrder(cancelledOrder);
      setIsError(false);
      setModalMessage("Pesanan berhasil dibatalkan!");
      setShowModal(true);
    } catch (error) {
      console.error("Error cancelling order:", error.message);
      setIsError(true);
      setModalMessage(`Gagal membatalkan pesanan: ${error.message}`);
      setShowModal(true);
    }
  };
  const getStatusMessage = (status) => {
    switch (status) {
      case "AWAITING_PAYMENT":
        return "Your order is awaiting payment. Please complete the payment to proceed.";
      case "PREPARING":
        return "Brewforce is preparing your order with care. Please wait a moment!";
      case "READY":
        return "Your order is ready! Please pick it up or wait for delivery.";
      case "COMPLETED":
        return "Thank you for ordering with Brewforce! We hope you enjoy your order.";
      case "CANCELLED":
        return "Your order has been cancelled. If this is a mistake, please contact us.";
      default:
        return "Status unknown. Please contact support for more information.";
    }
  };
  const getStatusIcon = (status) => {
    switch (status) {
      case "AWAITING_PAYMENT":
        return <FaClock className="inline mr-1 text-yellow-700" />;
      case "PREPARING":
        return <FaExclamationCircle className="inline mr-1 text-blue-700" />;
      case "READY":
        return <FaCheckCircle className="inline mr-1 text-green-700" />;
      case "COMPLETED":
        return <FaCheckCircle className="inline mr-1 text-teal-700" />;
      case "CANCELLED":
        return <FaTimesCircle className="inline mr-1 text-red-700" />;
      default:
        return null;
    }
  };
  return (
    <div className="mx-12 p-4 pb-32 pt-10">
       <a
        href="/orders"
        onClick={(e) => {
          e.preventDefault();
          window.history.back();
        }}
        className="text-amber-900  hover:text-gray-900 cursor-pointer text-m font-semibold  transition"
      >
         Back to Orders
      </a>
      <Modal />
      <h1 className="text-4xl md:text-5xl font-bold text-amber-900 pt-4  pb-8">Detail Pesanan</h1>
      <div className="bg-white p-6 shadow-md rounded-lg pb-6">
        <div className="mb-4">
        <p className="text-amber-900 font-semibold text-xl">
            Tanggal Pemesanan:{" "}
            <strong className="font-bold text-xl">{moment(order.createdDateTime).locale("id").format("D MMMM YYYY HH:mm")}</strong>
          </p>
          <p className="text-amber-900 font-semibold text-m pt-2">
            Status:{" "}
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
          </p>
        </div>
        <div className="space-y-2">
        <h2 className="text-xl font-bold text-amber-900">Daftar Menu</h2>
          {order.items.map((item, index) => (
            <div key={index} className="flex justify-between text-amber-700 ">
              <p>
                {item.quantity} x {item.menuName}
              </p>
             
            </div>
          ))}
        </div>
        <div className="mt-6 border-t border-gray-200 pt-4 text-amber-900 ">
          <div className="flex justify-between">
            <p className="text-lg font-bold">Total Harga</p>
            <p className="text-lg font-bold">Rp {order.totalHarga.toLocaleString()}</p>
          </div>
        </div>
        {/* Tombol Batalkan Pesanan */}
        {["AWAITING_PAYMENT"].includes(order.status) && (
          <button
            onClick={() => handleCancelOrder(order.idOrder)}
            className="mt-4 bg-red-600 text-white px-4 py-2 rounded-md hover:bg-red-700 transition w-full"
          >
            Batalkan Pesanan
          </button>
        )}
        <div className="mt-6 border-t border-gray-200 pt-4 text-amber-900 ">
         <p className="text-sm text-gray-600 italic">
          {getStatusIcon(order.status)}
          <span className={`font-bold ${{
            AWAITING_PAYMENT: "text-yellow-700",
            PREPARING: "text-blue-700",
            READY: "text-green-700",
            COMPLETED: "text-teal-700",
            CANCELLED: "text-red-700",
          }[order.status] || "text-gray-700"}`}>
            {getStatusMessage(order.status)}
          </span>
        </p>
        </div>
        </div>
      </div>

  );
};
export default OrderDetailPembeli;
