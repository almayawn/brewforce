"use client";
import React, { useState, useEffect } from "react";
import { useRouter } from "next/navigation";
import { jwtDecode } from "jwt-decode";

export default function MenusPage() {
  const [menus, setMenus] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [loaded, setLoaded] = useState(false);
  const [userRole, setUserRole] = useState(null);
  const [token, setToken] = useState(null);

  const router = useRouter();

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
      if (role === "PEMBELI") {
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

  const handleViewDetail = (menuId) => {
    router.push(`/menus/${menuId}`);
  };

  const handleEditMenu = (menuId, e) => {
    // Only ADMIN can edit menus
    if (userRole !== "ADMIN") return;
    
    e.stopPropagation(); // Prevent triggering the parent card's click event
    router.push(`/menus/edit/${menuId}`);
  };

  const handleCreateMenu = () => {
    // Only ADMIN can create new menus
    if (userRole !== "ADMIN") return;
    
    router.push('/menus/create');
  };

  if (loading) {
    return (
      <div className="min-h-screen bg-gradient-to-b from-amber-50 to-amber-100 font-poppins flex items-center justify-center">
        <div className="coffee-loading">
          <div className="coffee-cup"></div>
          <div className="coffee-steam steam-1"></div>
          <div className="coffee-steam steam-2"></div>
          <div className="coffee-steam steam-3"></div>
          <p className="mt-4 text-amber-800 font-medium">Loading menu data...</p>
        </div>
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
  }
  
  if (error) {
    return (
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
              : "Our beans seem to be encrypted too well..."}
          </p>
          {error.includes("Unauthorized") && (
            <button 
              onClick={() => router.push('/')}
              className="mt-4 px-4 py-2 bg-amber-800 text-white rounded hover:bg-amber-700 transition"
            >
              Go to Home
            </button>
          )}
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-b from-amber-50 to-amber-100 font-poppins">
      {/* Subtle security pattern overlay */}
      <div className="absolute inset-0 bg-repeat opacity-5 pattern-lock"></div>
      
      <div className="relative z-10 container mx-auto px-4 py-8 pb-24">
        <div className="text-center mb-10">
          <div className="mb-1 flex justify-center items-center">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 text-amber-800 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 110-4m0 4v2m0-6V4" />
            </svg>
            <h1 className="text-4xl font-bold text-amber-900">Menu Management</h1>
          </div>
          <div className="w-24 h-1 bg-amber-600 mx-auto mb-4"></div>
          <p className="text-amber-800 max-w-2xl mx-auto">
            {userRole === "ADMIN" 
              ? "Add, edit, and manage your coffee offerings from this secure dashboard." 
              : "View your coffee offerings from this secure dashboard."}
          </p>
          
          {/* Role indicator for better UX */}
          <div className="mt-2 inline-flex items-center px-3 py-1 rounded-full text-sm font-medium bg-amber-100 text-amber-800">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5.121 17.804A13.937 13.937 0 0112 16c2.5 0 4.847.655 6.879 1.804M15 10a3 3 0 11-6 0 3 3 0 016 0zm6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            {userRole} Mode
          </div>
        </div>
        
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
          {menus.map((menu, index) => (
            <div 
              key={menu.idMenu} 
              className={`bg-white rounded-lg shadow-md overflow-hidden border border-amber-200 hover:shadow-xl transition-all transform hover:-translate-y-1 
                ${loaded ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-8'}`}
              style={{ transitionDelay: `${150 * (index % 8)}ms`, transitionDuration: '800ms' }}
              onClick={() => handleViewDetail(menu.idMenu)}
            >
              <div className="h-48 overflow-hidden relative">
                <img 
                  src={menu.fotoPath || 'https://images.unsplash.com/photo-1444418776041-9c7e33cc5a9c'}
                  alt={menu.namaMenu} 
                  className="w-full h-full object-cover transform hover:scale-105 transition-transform duration-700"
                />
                
                {/* Edit button - ONLY show for ADMIN role */}
                {userRole === "ADMIN" && (
                  <button 
                    onClick={(e) => handleEditMenu(menu.idMenu, e)}
                    className="absolute top-2 right-2 bg-white p-2 rounded-full shadow-md hover:bg-amber-100 transition-colors transform hover:scale-110"
                    title="Edit Menu"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-amber-800" viewBox="0 0 20 20" fill="currentColor">
                      <path d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z" />
                    </svg>
                  </button>
                )}
                
                <div className="absolute top-2 left-2 bg-amber-800 text-white text-xs py-1 px-2 rounded-full">
                  <div className="flex items-center">
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-3 w-3 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z" />
                    </svg>
                    Secured
                  </div>
                </div>
              </div>
              
              <div className="p-4">
                <h2 className="font-bold text-xl mb-1 text-amber-900">{menu.namaMenu}</h2>
                <p className="text-amber-700 text-sm mb-3 line-clamp-2">{menu.deskripsiMenu || 'A delicious menu item with premium secured flavors.'}</p>
                
                <div className="flex justify-between items-center">
                  <p className="text-amber-900 font-bold text-lg">Rp {menu.hargaMenu.toLocaleString()}</p>
                  <p className="text-sm text-amber-700 font-medium">
                    <span className={`inline-block w-2 h-2 rounded-full ${menu.stok > 10 ? 'bg-green-500' : menu.stok > 0 ? 'bg-yellow-500' : 'bg-red-500'} mr-1`}></span>
                    Stok: {menu.stok}
                  </p>
                </div>
                
                <div className="mt-4">
                  <button
                    onClick={(e) => { e.stopPropagation(); handleViewDetail(menu.idMenu); }}
                    className="w-full bg-amber-800 text-white py-2 px-4 rounded hover:bg-amber-700 transition font-medium text-sm flex items-center justify-center"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4 mr-1" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                    </svg>
                    View Details
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
        
        {menus.length === 0 && (
          <div className="text-center py-16">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-16 w-16 mx-auto text-amber-300 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9.172 16.172a4 4 0 015.656 0M9 10h.01M15 10h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <h3 className="text-xl font-bold text-amber-800">No menu items found</h3>
            <p className="text-amber-700 mt-2">
              {userRole === "ADMIN" 
                ? "Click the + button to add your first menu item" 
                : "No menu items are available at this time."}
            </p>
          </div>
        )}
      </div>

      {/* Create Menu Button - ONLY show for ADMIN role */}
      {userRole === "ADMIN" && (
        <div className="fixed bottom-6 right-6 animate-bounce-subtle z-50">
          <button
            onClick={handleCreateMenu}
            className="bg-amber-800 text-white w-14 h-14 rounded-full shadow-lg hover:bg-amber-900 transition-colors transform hover:scale-110 flex items-center justify-center group"
            title="Add New Menu"
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8 transition-transform group-hover:rotate-90" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
            </svg>
          </button>
        </div>
      )}

      {/* Custom CSS for subtle patterns and animations */}
      <style jsx>{`
        .pattern-lock {
          background-image: url("data:image/svg+xml,%3Csvg width='20' height='20' viewBox='0 0 20 20' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M10 12a2 2 0 100-4 2 2 0 000 4zm0 2a4 4 0 110-8 4 4 0 010 8zm-2-4a2 2 0 11-4 0 2 2 0 014 0zm8 0a2 2 0 11-4 0 2 2 0 014 0z' fill='%23805500' fill-opacity='0.2' fill-rule='evenodd'/%3E%3C/svg%3E");
        }
        @keyframes bounce-subtle {
          0%, 100% {
            transform: translateY(0);
          }
          50% {
            transform: translateY(-10px);
          }
        }
        .animate-bounce-subtle {
          animation: bounce-subtle 3s ease-in-out infinite;
        }
      `}</style>
    </div>
  );
}