"use client";

import Image from "next/image";
import React, { useState } from "react";
import { useRouter } from "next/navigation";


const MenuDetailKasir = ({ menu: initialMenu }) => {
  const [menu, setMenu] = useState(initialMenu);
  const [showModal, setShowModal] = useState(false);
  const [newStok, setNewStok] = useState(menu.stok);
  const [loading, setLoading] = useState(false);

  const handleUpdateStok = async () => {
    setLoading(true);
    try {
      const storedToken = localStorage.getItem("token");

      const response = await fetch(
        `${process.env.NEXT_PUBLIC_API_URL_BE}/api/menus/${menu.idMenu}/stock`,
        {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${storedToken}`,
          },
          body: JSON.stringify({
            stok: newStok,
          }),
        }
      );

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || "Failed to update menu stock");
      }
      const res = await response.json();
      setMenu(res.data);
      setShowModal(false);
      alert("Stok menu berhasil diperbarui!");
    } catch (error) {
      console.error("Error updating stock menu:", error.message);
      alert(`Gagal memperbarui stok: ${error.message}`);
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
              className="bg-amber-800 hover:bg-amber-700 text-white px-4 py-2 rounded-md flex items-center"
              onClick={() => setShowModal(true)}
            >
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
              </svg>
              Update Stok
            </button>
          </div>
        </div>
      </div>

      {/* MODAL */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-40 flex items-center justify-center z-50">
          <div className="bg-white p-6 rounded-lg shadow-lg w-full max-w-sm">
            <h2 className="text-xl font-bold mb-4 text-amber-900">Perbarui Stok</h2>
            <form onSubmit={handleUpdateStok}>
              <input
                required
                type="number"
                value={newStok}
                onChange={(e) => setNewStok(e.target.value)}
                className="w-full border border-gray-300 rounded-md px-4 py-2 mb-4"
                min={0}
              />
              <div className="flex justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="px-4 py-2 rounded-md bg-gray-300 hover:bg-gray-400 text-gray-800"
                >
                  Batal
                </button>
                <button
                  type="submit"
                  disabled={loading}
                  className="px-4 py-2 rounded-md bg-amber-800 hover:bg-amber-700 text-white"
                >
                  {loading ? "Menyimpan..." : "Simpan"}
                </button>
              </div>
            </form>

          </div>
        </div>
  )
}
    </div >
  );
};

export default MenuDetailKasir;
