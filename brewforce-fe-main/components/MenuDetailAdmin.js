"use client";

import Image from "next/image";
import React, { useState } from "react";
import { useRouter } from "next/navigation";


const MenuDetailAdmin = ({ menu: initialMenu }) => {
  const [menu, setMenu] = useState(initialMenu);
  const [loading, setLoading] = useState(false);
  const [showModal, setShowModal] = useState(false); // Untuk modal konfirmasi
  const router = useRouter();

  const handleDelete = async () => {
    setLoading(true);
    try {
      const storedToken = localStorage.getItem("token");

      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL_BE}/api/menus/${menu.idMenu}`,
        {
          method: "DELETE",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${storedToken}`,
          },
        }
      );

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || "Failed to delete menu");
      }

      alert("Menu berhasil dihapus!");
      setShowModal(false);
      router.push("/menus");
    } catch (error) {
      console.error("Error deleting menu:", error.message);
      alert(`Gagal menghapus menu: ${error.message}`);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="mx-12 p-4 pb-32 pt-10">
      <a
        href="/menu"
        onClick={(e) => {
          e.preventDefault();
          window.history.back();
        }}
        className="text-amber-900 hover:text-gray-900 cursor-pointer text-m font-semibold transition"
      >
        Back to Menu List
      </a>

      <h1 className="text-4xl md:text-5xl font-bold text-amber-900 pt-4 pb-8">
        Detail Menu
      </h1>

      <div className="bg-white p-6 shadow-md rounded-lg pb-6 flex flex-col md:flex-row gap-6">
        <div className="w-full md:w-1/2">
        <img
            src={menu.fotoPath}
            alt={menu.namaMenu}
            className="w-full h-full object-cover transform hover:scale-105 transition-transform duration-700"
          />
        </div>
        <div className="w-full md:w-1/2 text-amber-900">
          <h2 className="text-3xl font-bold mb-2">{menu.namaMenu}</h2>
          <p className="text-lg italic mb-4 text-gray-600">{menu.deskripsiMenu}</p>
          <p className="text-xl font-bold mb-2">
            Harga: <span className="text-amber-800">Rp {menu.hargaMenu.toLocaleString("id-ID")}</span>
          </p>
          <p className="text-md font-semibold">
            Stok tersedia: <span className="text-green-700">{menu.stok}</span>
          </p>
          <div className="flex justify-end mb-6">
            <button
              onClick={() => setShowModal(true)}
              className="bg-red-500 hover:bg-red-600 text-white px-4 py-2 rounded-md flex items-center"
            >
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
              Hapus Menu
            </button>
          </div>
        </div>
      </div>

      {/* Modal konfirmasi */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-40 flex justify-center items-center z-50">
          <div className="bg-white p-6 rounded-lg shadow-md text-center w-80">
            <h2 className="text-xl font-semibold mb-4 text-amber-900">Konfirmasi Hapus</h2>
            <p className="mb-6 text-gray-700">Apakah kamu yakin ingin menghapus menu ini?</p>
            <div className="flex justify-center gap-4">
              <button
                onClick={() => setShowModal(false)}
                className="px-4 py-2 rounded-md bg-gray-300 text-gray-700 hover:bg-gray-400"
              >
                Batal
              </button>
              <button
                onClick={handleDelete}
                disabled={loading}
                className={`px-4 py-2 rounded-md text-white ${loading ? "bg-red-300" : "bg-red-600 hover:bg-red-700"}`}
              >
                {loading ? "Menghapus..." : "Hapus"}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default MenuDetailAdmin;
