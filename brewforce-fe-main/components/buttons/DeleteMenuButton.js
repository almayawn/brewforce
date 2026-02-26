import React from "react";

const DeleteMenuButton = ({ menuId, onDelete }) => {
  const handleDelete = () => {
    if (confirm("Apakah kamu yakin ingin menghapus menu ini?")) {
      onDelete(menuId);
    }
  };

  return (
    <button
      onClick={handleDelete}
      className="bg-red-500 text-white px-4 py-2 rounded"
    >
      Hapus Menu
    </button>
  );
};

export default DeleteMenuButton;
