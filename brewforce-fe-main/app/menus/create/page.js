'use client';
import React, { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { jwtDecode } from "jwt-decode";

export default function CreateMenuPage() {
  const [menu, setMenu] = useState({
    namaMenu: "",
    deskripsiMenu: "",
    hargaMenu: 0,
    stok: 0,
    fotoPath: "",
  });
  const [error, setError] = useState(null);
  const [isSaving, setIsSaving] = useState(false);
  const [loaded, setLoaded] = useState(true); // For animation
  const [toast, setToast] = useState({ show: false, type: '', message: '' });
  const [userRole, setUserRole] = useState(null);
  const router = useRouter();
  const [token, setToken] = useState(null);

  // Add this useEffect to check user role when component mounts
  useEffect(() => {
    const storedToken = localStorage.getItem('token');
    setToken(storedToken);

    if (!storedToken) {
      router.push('/login');
      return;
    }

    try {
      // Decode the JWT token
      const decodedToken = jwtDecode(storedToken);
      const role = decodedToken.role;
      setUserRole(role);
      
      // If user is not ADMIN, show unauthorized error and restrict access
      if (role !== "ADMIN") {
        setError("Unauthorized: Only Admin can create menu items");
      }
    } catch (err) {
      console.error("Error decoding token:", err);
      setError("Authentication failed");
    }
  }, [token]);
  
  const handleChange = (e) => {
    const { name, value } = e.target;
    
    // Handle numeric inputs
    if (name === "hargaMenu" || name === "stok") {
      // If the field is emptied entirely, store it as is (empty string)
      if (value === "") {
        setMenu(prev => ({ ...prev, [name]: "" }));
      } else {
        // Parse integer for non-empty values
        const numValue = parseInt(value, 10);
        
        // If parsing failed, don't update state
        if (isNaN(numValue)) {
          return;
        }
        
        // Otherwise update with the parsed integer
        setMenu(prev => ({ ...prev, [name]: numValue }));
      }
    } else {
      setMenu(prev => ({ ...prev, [name]: value }));
    }
  };

  const showToast = (type, message) => {
    setToast({ show: true, type, message });
    // Hide toast after 3 seconds
    setTimeout(() => {
      setToast({ show: false, type: '', message: '' });
    }, 3000);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    // Validate that fields are not empty
    // Create a copy of the menu data for submission
    const submitData = { ...menu };

    // Convert empty string fields to 0 for numeric fields
    if (submitData.hargaMenu === "") submitData.hargaMenu = 0;
    if (submitData.stok === "") submitData.stok = 0;
    
    // Validate that fields are not empty
    if (!submitData.namaMenu.trim()) {
      showToast('error', 'Nama menu tidak boleh kosong');
      return;
    }

    if (submitData.hargaMenu < 0) {
      showToast('error', 'Harga menu tidak boleh negatif');
      return;
    }

    if (submitData.stok < 0) {
      showToast('error', 'Stok tidak boleh negatif');
      return;
    }

    setIsSaving(true);
    
    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL_BE}/api/menus/`, {
        method: "POST",
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify(menu),
      });
      
      if (!response.ok) {
        throw new Error(`HTTP error! Status: ${response.status}`);
      }
      
      showToast('success', 'Menu berhasil dibuat!');
      
      // Redirect after a short delay to allow toast to be seen
      setTimeout(() => {
        router.push("/menus");
      }, 1500);
      
    } catch (err) {
      setError("Gagal membuat menu");
      setIsSaving(false);
      console.error("Error creating menu:", err);
      showToast('error', 'Gagal membuat menu');
    }
  };

  const handleCancel = () => {
    router.push("/menus");
  };

  const preventWheelChange = (e) => {
    // Prevent the default scroll behavior when the input is focused
    e.target.blur();
  };

  if (error) return (
    <div className="min-h-screen bg-gradient-to-b from-amber-50 to-amber-100 font-poppins flex items-center justify-center">
      <div className="text-center">
        <div className="text-red-600 mb-2">
          <svg xmlns="http://www.w3.org/2000/svg" className="h-12 w-12 mx-auto" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
          </svg>
        </div>
        <p className="text-xl font-bold text-amber-900">Error: {error}</p>
        <p className="text-amber-800 mt-2">
          {error.includes("Unauthorized") 
            ? "You don't have permission to access this page." 
            : "Our secure connection failed. Please try again."}
        </p>
        {error.includes("Unauthorized") && (
          <button 
            onClick={() => router.push('/')}
            className="mt-4 px-4 py-2 bg-amber-800 text-white rounded hover:bg-amber-700 transition"
          >
            Back to Home
          </button>
        )}
      </div>
    </div>
  );

  return (
    <div className="min-h-screen bg-gradient-to-b from-amber-50 to-amber-100 font-poppins">
      {/* Toast notification */}
      {toast.show && (
        <div className={`fixed top-4 right-4 z-50 px-6 py-3 rounded-lg shadow-lg flex items-center transition-all transform animate-fade-in
          ${toast.type === 'success' ? 'bg-green-600 text-white' : 'bg-red-600 text-white'}`}>
          <div className="mr-2">
            {toast.type === 'success' ? (
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
              </svg>
            ) : (
              <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
              </svg>
            )}
          </div>
          <p className="font-medium">{toast.message}</p>
        </div>
      )}

      {/* Subtle security pattern overlay */}
      <div className="absolute inset-0 bg-repeat opacity-5 pattern-lock"></div>
      
      <div className="relative z-10 container mx-auto px-4 py-8">
        <div className="text-center mb-6">
          <div className="mb-1 flex justify-center items-center">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 text-amber-800 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
            </svg>
            <h1 className="text-3xl font-bold text-amber-900">Add New Menu</h1>
          </div>
          <div className="w-24 h-1 bg-amber-600 mx-auto mb-4"></div>
          <p className="text-amber-800 max-w-2xl mx-auto">
            Create a new secured menu item with encrypted flavors
          </p>
        </div>
        
        <div className={`bg-white rounded-lg shadow-lg overflow-hidden border border-amber-200 transition-all duration-500 ${loaded ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-8'}`}>
          <div className="p-6">
            <form onSubmit={handleSubmit} className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div className="space-y-4">
                  <div>
                    <label htmlFor="namaMenu" className="block text-sm font-medium text-amber-900 mb-1 flex items-center">
                      <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                      </svg>
                      Menu Name
                    </label>
                    <input
                      type="text"
                      id="namaMenu"
                      name="namaMenu"
                      value={menu.namaMenu}
                      onChange={handleChange}
                      placeholder="Enter menu name"
                      required
                      className="w-full px-4 py-2 border border-amber-300 rounded-md focus:outline-none focus:ring-2 focus:ring-amber-500 text-amber-900 bg-amber-50"
                    />
                  </div>
                  
                  <div>
                    <label htmlFor="deskripsiMenu" className="block text-sm font-medium text-amber-900 mb-1 flex items-center">
                      <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h7" />
                      </svg>
                      Description
                    </label>
                    <textarea
                      id="deskripsiMenu"
                      name="deskripsiMenu"
                      value={menu.deskripsiMenu}
                      onChange={handleChange}
                      placeholder="Enter menu description"
                      rows={3}
                      className="w-full px-4 py-2 border border-amber-300 rounded-md focus:outline-none focus:ring-2 focus:ring-amber-500 text-amber-900 bg-amber-50"
                    />
                  </div>
                  
                  <div>
                    <label htmlFor="hargaMenu" className="block text-sm font-medium text-amber-900 mb-1 flex items-center">
                      <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                      </svg>
                      Price (Rp)
                    </label>
                    <input
                      type="number"
                      id="hargaMenu"
                      name="hargaMenu"
                      value={menu.hargaMenu}
                      onChange={handleChange}
                      onWheel={preventWheelChange}
                      placeholder="0"
                      required
                      min="0"
                      step="500"
                      className="w-full px-4 py-2 border border-amber-300 rounded-md focus:outline-none focus:ring-2 focus:ring-amber-500 text-amber-900 bg-amber-50"
                    />
                  </div>
                  
                  <div>
                    <label htmlFor="stok" className="block text-sm font-medium text-amber-900 mb-1 flex items-center">
                      <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 12l3-3m0 0l3 3m-3-3v12m6-6l-3 3m0 0l-3-3m3 3V6" />
                      </svg>
                      Stock
                    </label>
                    <input
                      type="number"
                      id="stok"
                      name="stok"
                      value={menu.stok}
                      onChange={handleChange}
                      onWheel={preventWheelChange}
                      placeholder="0"
                      required
                      min="0"
                      className="w-full px-4 py-2 border border-amber-300 rounded-md focus:outline-none focus:ring-2 focus:ring-amber-500 text-amber-900 bg-amber-50"
                    />
                  </div>
                  
                  <div>
                    <label htmlFor="fotoPath" className="block text-sm font-medium text-amber-900 mb-1 flex items-center">
                      <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                      </svg>
                      Image URL
                    </label>
                    <input
                      type="text"
                      id="fotoPath"
                      name="fotoPath"
                      value={menu.fotoPath}
                      onChange={handleChange}
                      placeholder="https://example.com/image.jpg"
                      className="w-full px-4 py-2 border border-amber-300 rounded-md focus:outline-none focus:ring-2 focus:ring-amber-500 text-amber-900 bg-amber-50"
                    />
                  </div>
                </div>
                
                <div className="flex flex-col space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-amber-900 mb-1 flex items-center">
                      <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                      </svg>
                      Image Preview
                    </label>
                    <div className="border border-amber-200 rounded-lg h-64 overflow-hidden bg-amber-50 flex items-center justify-center shadow-inner">
                      {menu.fotoPath ? (
                        <img
                          src={menu.fotoPath}
                          alt={menu.namaMenu || "Menu preview"}
                          className="w-full h-full object-contain"
                        />
                      ) : (
                        <div className="text-amber-700 text-center px-4 font-medium flex flex-col items-center">
                          <svg xmlns="http://www.w3.org/2000/svg" className="h-12 w-12 mb-2 text-amber-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                          </svg>
                          Image preview will appear here
                        </div>
                      )}
                    </div>
                  </div>
                  
                  <div className="p-4 bg-amber-50 rounded-lg border border-amber-200 shadow-sm">
                    <div className="flex items-center mb-2">
                      <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-amber-800" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                      </svg>
                      <p className="font-medium text-amber-900 ml-2">Security Information</p>
                    </div>
                    <ul className="text-sm text-amber-700 space-y-1 pl-6 list-disc">
                      <li>Use high-quality images for better presentation</li>
                      <li>Ideal size: 600 x 400 pixels</li>
                      <li>Items will be encrypted in our secure database</li>
                      <li>All menu changes are logged for security purposes</li>
                    </ul>
                  </div>
                </div>
              </div>
              
              <div className="flex justify-end space-x-4 pt-4 border-t border-amber-200">
                <button
                  type="button"
                  onClick={handleCancel}
                  className="px-6 py-2 border border-amber-300 rounded-md shadow-sm text-sm font-medium text-amber-800 bg-white hover:bg-amber-50 transition focus:outline-none focus:ring-2 focus:ring-amber-500 flex items-center"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                  Cancel
                </button>
                <button
                  type="submit"
                  className="px-6 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-amber-800 hover:bg-amber-700 transition focus:outline-none focus:ring-2 focus:ring-amber-500 flex items-center"
                  disabled={isSaving}
                >
                  {isSaving ? (
                    <>
                      <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                      </svg>
                      Creating...
                    </>
                  ) : (
                    <>
                      <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                      </svg>
                      Create Menu
                    </>
                  )}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>

      {/* Custom CSS for subtle patterns and animations */}
      <style jsx>{`
        .pattern-lock {
          background-image: url("data:image/svg+xml,%3Csvg width='20' height='20' viewBox='0 0 20 20' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M10 12a2 2 0 100-4 2 2 0 000 4zm0 2a4 4 0 110-8 4 4 0 010 8zm-2-4a2 2 0 11-4 0 2 2 0 014 0zm8 0a2 2 0 11-4 0 2 2 0 014 0z' fill='%23805500' fill-opacity='0.2' fill-rule='evenodd'/%3E%3C/svg%3E");
        }
        
        @keyframes fade-in {
          from {
            opacity: 0;
            transform: translateY(-20px);
          }
          to {
            opacity: 1;
            transform: translateY(0);
          }
        }
        
        .animate-fade-in {
          animation: fade-in 0.3s ease-out forwards;
        }
      `}</style>
    </div>
  );
}