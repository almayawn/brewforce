"use client";

import React, { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { jwtDecode } from "jwt-decode";

export default function CreateOrderPage() {
  const [menus, setMenus] = useState([]);
  const [orderItems, setOrderItems] = useState([]);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [loaded, setLoaded] = useState(false);
  const [loading, setLoading] = useState(true);
  const router = useRouter();
  const [userRole, setUserRole] = useState(null);
  const [token, setToken] = useState(null);
  const [addedMenus, setAddedMenus] = useState([]);


  useEffect(() => {
    const storedToken = localStorage.getItem('token');
    setToken(storedToken);

    if (!storedToken) {
      router.push('/login');
      return;
    }

    try {
      // Decode JWT token to get user role
      const decodedToken = jwtDecode(storedToken);
      const role = decodedToken.role;
      setUserRole(role);

      // If user is PEMBELI, show unauthorized and return
      if (role === "ADMIN" || role === "KASIR") {
        setLoading(false);
        setError("Unauthorized: Only authorized roles can access this page");
        return;
      }

      const fetchMenus = async () => {
        try {
          const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL_BE}/api/menus/`, {
            headers: {
              "Authorization": `Bearer ${storedToken}`,
              "Content-Type": "application/json",
            },
          });

          if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
          }

          const data = await response.json();

          const sortedMenus = [...data].sort((a, b) =>
            a.namaMenu.localeCompare(b.namaMenu, 'id', { sensitivity: 'base' })
          );

          setMenus(sortedMenus);
          setLoading(false);

          // Trigger animations after data is loaded
          setTimeout(() => setLoaded(true), 300);
        } catch (err) {
          setError("Failed to fetch menus");
          setLoading(false);
          console.error("Error fetching menus:", err);
        }
      };

      // Proceed with fetching menus for ADMIN and KASIR roles
      fetchMenus();
    } catch (err) {
      console.error("Error decoding token:", err);
      setError("Authentication failed");
      setLoading(false);
    }
  }, []);

  const handleAddToOrder = (menuId) => {
    if (!addedMenus.includes(menuId)) {
      setAddedMenus(prev => [...prev, menuId]);
    }

    setOrderItems((prevItems) => {
      const existingItem = prevItems.find((item) => item.idMenu === menuId);
      if (existingItem) {
        return prevItems.map((item) =>
          item.idMenu === menuId ? { ...item, quantity: item.quantity + 1 } : item
        );
      }
      return [...prevItems, { idMenu: menuId, quantity: 1 }];
    });
  };

  const handleQuantityChange = (menuId, quantity) => {
    const intQty = parseInt(quantity);
    if (isNaN(intQty) || intQty < 1) return;
    setOrderItems((prevItems) =>
      prevItems.map((item) =>
        item.idMenu === menuId ? { ...item, quantity: intQty } : item
      )
    );
  };

  const handleRemoveFromOrder = (idMenu) => {
    setOrderItems(orderItems.filter(item => item.idMenu !== idMenu));
    setAddedMenus(addedMenus.filter(id => id !== idMenu));
  };



  const handleSubmitOrder = async () => {
    const token = localStorage.getItem("token");
    if (!token) {
      setError("Token tidak ditemukan.");
      return;
    }

    const payload = {
      menuItems: orderItems.map(item => ({
        menuId: item.idMenu,
        quantity: item.quantity
      }))
    };

    try {
      const res = await fetch(`${process.env.NEXT_PUBLIC_API_URL_BE}/api/orders/`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(payload),
      });

      if (!res.ok) {
        const errorData = await res.text();
        throw new Error(errorData || "Failed to order");
      }
      setSuccess("Pesanan berhasil dibuat!");
      const data = await res.json();
      const orderId = data.idOrder;
      setOrderItems([]);
      router.push(`/orders/${orderId}`);
    } catch (error) {
      console.error(error.message);
      alert(`${error.message}`);
    }
  };


  return (
    <div className="min-h-screen bg-amber-50 p-6 font-poppins">
      <div className="max-w-4xl mx-auto">
        <h1 className="text-3xl font-bold text-amber-900 mb-6">Pilih Menu</h1>

        {error && <p className="text-red-600 mb-4">{error}</p>}
        {success && <p className="text-green-600 mb-4">{success}</p>}

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {menus
            .filter((menu) => menu.stok > 0)
            .map((menu, index) => (
              <div
                key={menu.idMenu}
                className={`bg-white rounded-lg shadow-md overflow-hidden border border-amber-200 hover:shadow-xl transition-all transform hover:-translate-y-1 
                        ${loaded ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-8'}`}
                style={{ transitionDelay: `${150 * (index % 8)}ms`, transitionDuration: '800ms' }}
              >

                <div className="h-48 overflow-hidden relative">
                  <img
                    src={menu.fotoPath || 'https://images.unsplash.com/photo-1444418776041-9c7e33cc5a9c'}
                    alt={menu.namaMenu}
                    className="w-full h-full object-cover transform hover:scale-105 transition-transform duration-700"
                  /></div>
                <div key={menu.idMenu} className="bg-white p-4 shadow rounded-lg">
                  <h2 className="text-lg font-semibold text-amber-800">{menu.namaMenu}</h2>
                  <p className="text-sm text-gray-600 mb-2">Harga: Rp {menu.hargaMenu}</p>

                  <div className="mt-2">
                    {!addedMenus.includes(menu.idMenu) ? (
                      <button
                        onClick={() => handleAddToOrder(menu.idMenu)}
                        className="bg-amber-800 text-white py-1 px-3 rounded hover:bg-amber-700 transition font-medium text-sm"
                      >
                        Add to Order
                      </button>
                    ) : (
                      <div className="flex items-center gap-2">
                        <input
                          type="number"
                          min="1"
                          value={
                            orderItems.find((item) => item.idMenu === menu.idMenu)?.quantity || 1
                          }
                          onChange={(e) =>
                            handleQuantityChange(menu.idMenu, e.target.value)
                          }
                          className="w-16 px-2 py-1 border border-gray-300 rounded text-sm"
                        />
                        <button
                          onClick={() => handleRemoveFromOrder(menu.idMenu)}
                          className="text-red-600 text-sm hover:underline"
                        >
                          Remove
                        </button>
                      </div>
                    )}
                  </div>



                </div>
              </div>
            ))}
        </div>


        <div className="mt-8 bg-white shadow rounded-lg p-4">
          <h2 className="text-xl font-bold mb-4 text-amber-900">Pesanan Anda</h2>
          {orderItems.length === 0 ? (
            <p className="text-gray-600">Belum ada menu yang ditambahkan.</p>
          ) : (
            <>
              <ul className="mb-4">
                {orderItems.map((item, index) => {
                  const menu = menus.find((m) => m.idMenu === item.idMenu);
                  return (
                    <li key={index} className="flex justify-between mb-2 text-amber-700">
                      <span>{menu?.namaMenu || "Menu tidak ditemukan"}</span>
                      <span>x {item.quantity}</span>
                    </li>
                  );
                })}
              </ul>

              <div className="text-amber-900 mt-2 border-t border-gray-200 py-4">
                <div className="flex justify-between">
                  <p className="text-lg font-semibold">Total Harga</p>
                  <p className="text-lg font-semibold">
                    Rp{" "}
                    {orderItems
                      .reduce((total, item) => {
                        const menu = menus.find((m) => m.idMenu === item.idMenu);
                        return total + (menu?.hargaMenu || 0) * item.quantity;
                      }, 0)
                      .toLocaleString()}
                  </p>
                </div>
              </div>
            </>
          )}


          <button
            onClick={handleSubmitOrder}
            className="bg-amber-900 text-white py-2 px-4 rounded hover:bg-amber-800 transition"
            disabled={orderItems.length === 0}
          >
            Buat Pesanan
          </button>
        </div>
      </div>
    </div>
  );
}
