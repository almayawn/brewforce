"use client";
import { useState } from "react";

const UpdateStokButton = ({ menuId, onUpdate }) => {
  const [showModal, setShowModal] = useState(false);
  const [stok, setStok] = useState("");

  const handleSubmit = () => {
    if (stok === "" || isNaN(stok)) {
      alert("Masukkan angka stok yang valid!");
      return;
    }
    onUpdate(menuId, parseInt(stok));
    setShowModal(false);
    setStok(""); // Reset input
  };

  return (
    <>
      {/* Button untuk membuka modal */}
      <button
        onClick={() => setShowModal(true)}
        className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600 transition"
      >
        Update Stok
      </button>

      {/* Modal */}
      {showModal && (
        <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50">
          <div className="bg-white rounded-lg shadow-lg p-6 w-80">
            <h3 className="text-lg font-semibold text-gray-800 mb-4">
              Update Stok
            </h3>

            <input
              type="number"
              value={stok}
              onChange={(e) => setStok(e.target.value)}
              placeholder="Masukkan stok baru"
              className="w-full px-3 py-2 border border-gray-300 text-gray-900 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
            />

            <div className="flex justify-end gap-2 mt-4">
              <button
                onClick={() => setShowModal(false)}
                className="px-4 py-2 bg-gray-300 rounded hover:bg-gray-400 transition"
              >
                Batal
              </button>
              <button
                onClick={handleSubmit}
                className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600 transition"
              >
                Simpan
              </button>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default UpdateStokButton;
