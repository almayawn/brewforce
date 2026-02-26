'use client';

import React, { useState, useEffect } from 'react';

const OrdersPage = ({orders: initialOrders}) => {
    const [orders, setOrders] = useState([]);
    const [filteredOrders, setFilteredOrders] = useState([]);
    const [activeTab, setActiveTab] = useState('All');
    const [error, setError] = useState(null);
    const [loaded, setLoaded] = useState(false);
    const [loading, setLoading] = useState(true);
    const [toast, setToast] = useState({ show: false, type: '', message: '' });

    const [currentPage, setCurrentPage] = useState(1);
    const ordersPerPage = 10;

    const token = localStorage.getItem("token");

    const [sortConfig, setSortConfig] = useState({
        key: 'createdDateTime',
        direction: 'desc'
    });

    useEffect(() => {
        if (initialOrders && initialOrders.length > 0) {
            console.log("orders masuk")
            setLoading(false);
        }
    }, [initialOrders]);

    const tabs = ['All', 'AWAITING_PAYMENT', 'PREPARING', 'READY', 'COMPLETED', 'CANCELLED'];

    // Toast notification function
    const showToast = (type, message) => {
        setToast({ show: true, type, message });
        // Auto hide toast after 3 seconds
        setTimeout(() => {
            setToast({ show: false, type: '', message: '' });
        }, 3000);
    };

    useEffect(() => {
        setOrders(initialOrders);
        setFilteredOrders(initialOrders);
        setTimeout(() => setLoaded(true), 300);
    }, [initialOrders]);

    const handleTabChange = async (tab) => {
        setCurrentPage(1);
        setActiveTab(tab);
        setError(null); // Reset error state
        
        if (tab === 'All') {
            setFilteredOrders(orders);
            return;
        }
    
        try {
            setLoading(true); // Add loading state while fetching
            const response = await fetch(
                `${process.env.NEXT_PUBLIC_API_URL_BE}/api/orders?statuses=${tab}`, 
                {
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    }
                }
            );
    
            const filteredData = await response.json();
            if (!response.ok) {
                setError(filteredData.message);
            }else{
                setFilteredOrders(filteredData);
                setError(null);
            }
        } catch (error) {
            console.error('Error fetching filtered orders:', error);
            setError(error.message);
            setFilteredOrders([]); // Clear filtered orders on error
        } finally {
            setLoading(false);
        }
    };

    const paginate = (orders) => {
        const indexOfLastOrder = currentPage * ordersPerPage;
        const indexOfFirstOrder = indexOfLastOrder - ordersPerPage;
        return orders.slice(indexOfFirstOrder, indexOfLastOrder);
    };

    // Add this component for pagination controls
    const PaginationControls = ({ totalOrders }) => {
        const pageNumbers = Math.ceil(totalOrders / ordersPerPage);
        
        return (
            <div className="flex justify-center items-center gap-2 mt-6">
                <button
                    onClick={() => setCurrentPage(prev => Math.max(prev - 1, 1))}
                    disabled={currentPage === 1}
                    className={`px-4 py-2 rounded-lg transition-all duration-200 
                        ${currentPage === 1 
                            ? 'bg-gray-200 text-gray-500 cursor-not-allowed' 
                            : 'bg-amber-800 text-white hover:bg-amber-700'}`}
                >
                    Previous
                </button>
                
                <div className="flex gap-2">
                    {[...Array(pageNumbers)].map((_, idx) => (
                        <button
                            key={idx + 1}
                            onClick={() => setCurrentPage(idx + 1)}
                            className={`px-4 py-2 rounded-lg transition-all duration-200 
                                ${currentPage === idx + 1 
                                    ? 'bg-amber-800 text-white' 
                                    : 'bg-white text-amber-800 hover:bg-amber-100'}`}
                        >
                            {idx + 1}
                        </button>
                    ))}
                </div>
                
                <button
                    onClick={() => setCurrentPage(prev => Math.min(prev + 1, pageNumbers))}
                    disabled={currentPage === pageNumbers}
                    className={`px-4 py-2 rounded-lg transition-all duration-200 
                        ${currentPage === pageNumbers 
                            ? 'bg-gray-200 text-gray-500 cursor-not-allowed' 
                            : 'bg-amber-800 text-white hover:bg-amber-700'}`}
                >
                    Next
                </button>

                <span className="text-amber-800 ml-4">
                    Page {currentPage} of {pageNumbers}
                </span>
            </div>
        );
    };

    const getStatusColor = (status) => {
        const colors = {
        'AWAITING_PAYMENT': 'bg-yellow-100 text-yellow-800',
        'PREPARING': 'bg-blue-100 text-blue-800',
        'READY': 'bg-purple-100 text-purple-800',
        'COMPLETED': 'bg-green-100 text-green-800',
        'CANCELLED': 'bg-red-100 text-red-800'
        };
        return colors[status] || 'bg-amber-100 text-amber-800';
    };

    const getNextStatus = (currentStatus) => {
        const statusFlow = {
            'AWAITING_PAYMENT': 'PREPARING',
            'PREPARING': 'READY',
            'READY': 'COMPLETED',
            'COMPLETED': null,
            'CANCELLED': null
        };
        return statusFlow[currentStatus] || null;
    };

    const updateOrderStatus = async (orderId, nextStatus) => {
        try {
            const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL_BE}/api/orders/${orderId}/status`, {
                method: "PUT",
                headers: {
                  "Content-Type": "application/json",
                  Authorization: `Bearer ${token}`,
                },
                body: JSON.stringify({
                  idOrder: orderId,
                  status: nextStatus,
                }),
              });

            if (!response.ok) {
                throw new Error(`Error: ${response.status}`);
            }

            // Update local state instead of fetching
            setOrders(prevOrders => {
                const updatedOrders = prevOrders.map(order => 
                order.idOrder === orderId ? { ...order, status: nextStatus } : order
                );
                setFilteredOrders(updatedOrders.filter(
                activeTab === 'All' ? () => true : order => order.status === activeTab
                ));
                return updatedOrders;
            });
            
            // Show success toast notification
            showToast('success', `Order updated to ${nextStatus.replace('_', ' ')}`);
        } catch (error) {
            // Show error toast notification
            showToast('error', `Failed to update order: ${error.message}`);
            setError(error.message);
        }
    };

    const sortOrders = (orders) => {
        if (!sortConfig.key) return orders;
    
        return [...orders].sort((a, b) => {
            if (a[sortConfig.key] < b[sortConfig.key]) {
                return sortConfig.direction === 'asc' ? -1 : 1;
            }
            if (a[sortConfig.key] > b[sortConfig.key]) {
                return sortConfig.direction === 'asc' ? 1 : -1;
            }
            return 0;
        });
    };

    const handleSort = () => {
        setSortConfig({
            key: 'createdDateTime',
            direction: sortConfig.direction === 'asc' ? 'desc' : 'asc'
        });
    };

    if (loading) {
        return (
        <div className="min-h-screen bg-gradient-to-b from-amber-50 to-amber-100 font-poppins flex items-center justify-center">
            <div className="text-center">
            <div className="animate-spin h-12 w-12 mb-4 border-4 border-amber-800 border-t-transparent rounded-full mx-auto"></div>
            <p className="text-amber-800">Loading orders...</p>
            </div>
        </div>
        );
    }

return (
    <div className="min-h-screen bg-gradient-to-b from-amber-50 to-amber-100 p-6">
        {/* Toast notification */}
        {toast.show && (
            <div 
                className={`fixed top-4 right-4 z-50 px-6 py-3 rounded-lg shadow-lg flex items-center transition-all transform animate-fade-in
                    ${toast.type === 'success' ? 'bg-green-600 text-white' : 'bg-red-600 text-white'}`}
            >
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

        <div className="max-w-7xl mx-auto">

            <div className="text-center mb-10">
                <div className="mb-1 flex justify-center items-center">
                    <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6 text-amber-800 mr-2" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
                    </svg>
                    <h1 className="text-4xl font-bold text-amber-900">Secure Order Management</h1>
                </div>
                <div className="w-24 h-1 bg-amber-600 mx-auto mb-4"></div>
            </div>

            <div className="mb-6 flex flex-wrap justify-center gap-2">
                {tabs.map((tab) => (
                    <button
                        key={tab}
                        onClick={() => handleTabChange(tab)}
                        className={`px-4 py-2 rounded-lg transition-all duration-200 ${
                            activeTab === tab 
                            ? 'bg-amber-800 text-white shadow-lg' 
                            : 'bg-white text-amber-800 hover:bg-amber-100'
                        }`}
                    >
                        {tab.replace('_', ' ')}
                    </button>
                ))}
            </div>

            <div className="bg-white rounded-lg shadow-lg overflow-hidden">
                <table className="w-full">
                    <thead className="bg-amber-800 text-white">
                        <tr>
                            <th 
                                className="px-6 py-4 text-left cursor-pointer hover:bg-amber-700 transition-colors group"
                                onClick={handleSort}
                            >
                                <div className="flex items-center">
                                    Created At
                                    <span className="ml-2">
                                        {sortConfig.direction === 'asc' ? (
                                            <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 15l7-7 7 7" />
                                            </svg>
                                        ) : (
                                            <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                                            </svg>
                                        )}
                                    </span>
                                </div>
                            </th>
                            <th className="px-6 py-4 text-left">Customer</th>
                            <th className="px-6 py-4 text-left">Status</th>
                            <th className="px-6 py-4 text-left">Total</th>
                            <th className="px-6 py-4 text-left">Actions</th>
                        </tr>
                    </thead>
                    <tbody>
                    {error ? (
                        <tr>
                            <td colSpan="5" className="px-6 py-8 text-center">
                                <div className="flex flex-col items-center justify-center text-amber-800">
                                    <svg xmlns="http://www.w3.org/2000/svg" className="h-12 w-12 text-amber-600 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                                    </svg>
                                    <p className="text-lg font-medium">Error loading orders</p>
                                    <p className="text-sm opacity-75">{error}</p>
                                </div>
                            </td>
                        </tr>
                    ) : !filteredOrders || filteredOrders.length === 0 ? (
                        <tr>
                            <td colSpan="5" className="px-6 py-8 text-center">
                                <div className="flex flex-col items-center justify-center text-amber-800">
                                    <svg xmlns="http://www.w3.org/2000/svg" className="h-12 w-12 text-amber-600 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
                                    </svg>
                                    <p className="text-lg font-medium">No orders found</p>
                                    <p className="text-sm opacity-75">Try selecting a different status filter</p>
                                </div>
                            </td>
                        </tr>
                    ) : (
                        paginate(sortOrders(filteredOrders)).map((order, index) => (
                            <tr 
                                key={order.idOrder}
                                className={`border-b border-amber-100 hover:bg-amber-50 transition-colors ${
                                    loaded ? 'opacity-100' : 'opacity-0'
                                }`}
                                style={{ transitionDelay: `${index * 100}ms` }}
                            >
                                <td className="px-6 py-4">
                                    <span className="font-semibold text-amber-900">
                                        {new Date(order.createdDateTime).toLocaleString('en-GB', {
                                            day: 'numeric',
                                            month: 'long',
                                            year: 'numeric',
                                            hour: '2-digit',
                                            minute: '2-digit',
                                            hour12: false
                                        }).replace(' at', '')}
                                    </span>
                                </td>
                                <td className="px-6 py-4 text-amber-700">{order.username}</td>
                                <td className="px-6 py-4">
                                    <span className={`px-3 py-1 rounded-full text-sm font-medium inline-block ${getStatusColor(order.status)}`}>
                                        {order.status.replace('_', ' ')}
                                    </span>
                                </td>
                                <td className="px-6 py-4 font-semibold text-amber-900">
                                    Rp {order.totalHarga?.toLocaleString()}
                                </td>
                                <td className="px-6 py-4 flex gap-2">
                                    <a
                                        href={`/orders/${order.idOrder}`}
                                        key={order.idOrder}
                                        className="flex items-center gap-2 px-4 py-2 bg-amber-800 text-white rounded-md hover:bg-amber-700 transition-colors"
                                    >
                                        <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                                        </svg>
                                        Details
                                    </a>
                                    
                                    {getNextStatus(order.status) && (
                                        <button
                                            onClick={() => updateOrderStatus(order.idOrder, getNextStatus(order.status))}
                                            className={`flex items-center gap-2 px-4 py-2 rounded-md transition-colors
                                                ${order.status === 'COMPLETED' || order.status === 'CANCELLED'
                                                    ? 'bg-gray-300 cursor-not-allowed'
                                                    : 'bg-green-600 hover:bg-green-700 text-white'
                                                }`}
                                            disabled={order.status === 'COMPLETED' || order.status === 'CANCELLED'}
                                        >
                                            <svg xmlns="http://www.w3.org/2000/svg" className="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                                            </svg>
                                            {getNextStatus(order.status)?.replace('_', ' ')}
                                        </button>
                                    )}
                                </td>
                            </tr>
                        ))
                    
                    )}
                    </tbody>
                </table>
            </div>
                            {/* Add pagination controls */}
                            {!error && filteredOrders.length > 0 && (
                    <PaginationControls totalOrders={filteredOrders.length} />
                )}
        </div>

        {/* CSS for toast animation */}
        <style jsx>{`
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
};

export default OrdersPage;