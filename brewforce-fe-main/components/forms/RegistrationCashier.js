'use client';

import React, { useState, useEffect } from "react";
import { useRouter } from 'next/navigation';

const RegistrationCashier = ({ token }) => {
    const router = useRouter();
    const [formData, setFormData] = useState({
        username: "",
        password: "",
        name: "",
        role: "KASIR", // Default role is 'KASIR'
    });
    const [error, setError] = useState("");
    const [successMessage, setSuccessMessage] = useState("");
    const [loaded, setLoaded] = useState(false);
    const [showPassword, setShowPassword] = useState(false);
    const [isSubmitting, setIsSubmitting] = useState(false);

    useEffect(() => {
        setLoaded(true);
    }, []);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData((prevState) => ({ ...prevState, [name]: value }));
        if (error || successMessage) {
        setError("");
        setSuccessMessage("");
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setIsSubmitting(true);
        setError("");
        setSuccessMessage("");

        try {
            if (!token) {
                setError("Authorization token is missing. Please log in.");
                return;
            }

            const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL_AUTH}/api/users`, {
                method: "POST",
                headers: { 
                    "Content-Type": "application/json",
                    "Authorization": `Bearer ${token}` // Add Authorization header
                },
                body: JSON.stringify(formData),
            });
            
            const data = await response.json();
            
            if (response.ok) {
                setSuccessMessage("Registration successful! Please log in.");
                setFormData({ username: "", password: "", name: "", role: "KASIR" }); // Clear form
                setError(""); 
                setTimeout(() => {
                    router.push('/cashier');
                }, 2000);
            } else {
                setError(data.message || "Registration failed");
            }
        } catch (error) {
            setError("Error submitting form");
            console.error("Error registering:", error);
        }finally {
            setIsSubmitting(false);
        }
        
    };

return (
    <div className="min-h-screen bg-gradient-to-b from-amber-50 to-amber-100 font-poppins">
        {/* Background pattern */}
        <div className="absolute inset-0 bg-repeat opacity-5 pattern-lock"></div>

        <div className="relative z-10 container mx-auto px-4 py-12">
            <div className="max-w-xl mx-auto">
                {/* Registration Form */}
                <div className={`transition-all duration-700 delay-300 ${loaded ? 'translate-x-0 opacity-100' : 'translate-x-10 opacity-0'}`}>
                    <div className="bg-white rounded-lg shadow-xl p-8 border border-amber-200">
                        <h1 className="text-3xl font-bold text-amber-900 mb-6">Create Account</h1>
                        <form onSubmit={handleSubmit} className="space-y-6">
                            <div>
                                <label htmlFor="name" className="block text-sm font-medium text-amber-900 mb-2">
                                    Full Name
                                </label>
                                <input
                                    type="text"
                                    id="name"
                                    name="name"
                                    className="w-full px-4 py-3 rounded-md border border-amber-200 focus:outline-none focus:ring-2 focus:ring-amber-500 focus:border-amber-500"
                                    placeholder="John Doe"
                                    value={formData.name}
                                    onChange={handleChange}
                                    required
                                />
                            </div>

                            <div>
                                <label htmlFor="username" className="block text-sm font-medium text-amber-900 mb-2">
                                    Username
                                </label>
                                <input
                                    type="text"
                                    id="username"
                                    name="username"
                                    className="w-full px-4 py-3 rounded-md border border-amber-200 focus:outline-none focus:ring-2 focus:ring-amber-500 focus:border-amber-500"
                                    placeholder="yourusername"
                                    value={formData.username}
                                    onChange={handleChange}
                                    required
                                />
                            </div>

                            <div>
                                <label htmlFor="password" className="block text-sm font-medium text-amber-900 mb-2">
                                    Password
                                </label>
                                <div className="relative">
                                    <input
                                        type={showPassword ? "text" : "password"}
                                        id="password"
                                        name="password"
                                        className="w-full px-4 py-3 rounded-md border border-amber-200 focus:outline-none focus:ring-2 focus:ring-amber-500 focus:border-amber-500"
                                        placeholder="••••••••"
                                        value={formData.password}
                                        onChange={handleChange}
                                        required
                                    />
                                    <button
                                        type="button"
                                        className="absolute inset-y-0 right-0 flex items-center pr-3"
                                        onClick={() => setShowPassword(!showPassword)}
                                    >
                                        {showPassword ? (
                                            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-amber-700" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.88 9.88l-3.29-3.29m7.532 7.532l3.29 3.29M3 3l3.59 3.59m0 0A9.953 9.953 0 0112 5c4.478 0 8.268 2.943 9.543 7a10.025 10.025 0 01-4.132 5.411m0 0L21 21" />
                                            </svg>
                                        ) : (
                                            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-amber-700" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                                                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                                            </svg>
                                        )}
                                    </button>
                                </div>
                            </div>

                            {error && (
                                <div className="bg-red-50 text-red-800 rounded-md p-4">
                                    {error}
                                </div>
                            )}

                            {successMessage && (
                                <div className="bg-green-50 text-green-800 rounded-md p-4">
                                    {successMessage}
                                </div>
                            )}

                            <button
                                type="submit"
                                disabled={isSubmitting}
                                className={`w-full py-3 px-4 bg-amber-800 text-white rounded-md transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-amber-500 ${
                                isSubmitting ? 'opacity-75 cursor-not-allowed' : 'hover:bg-amber-700'
                                }`}
                            >
                                {isSubmitting ? 'Creating Account...' : 'Create Account'}
                            </button>

                            <div className="text-center mt-4">
                                <p className="text-amber-800">
                                    Want to manage cashier accounts?{' '}
                                    <a 
                                        href="/cashier" 
                                        className="font-medium text-amber-900 hover:text-amber-700 underline transition-colors"
                                    >
                                        Back to Account Management
                                    </a>
                                </p>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>

        <style jsx>{`
            .pattern-lock {
                background-image: url("data:image/svg+xml,%3Csvg width='20' height='20' viewBox='0 0 20 20' xmlns='http://www.w3.org/2000/svg'%3E%3Cpath d='M10 12a2 2 0 100-4 2 2 0 000 4zm0 2a4 4 0 110-8 4 4 0 010 8zm-2-4a2 2 0 11-4 0 2 2 0 014 0zm8 0a2 2 0 11-4 0 2 2 0 014 0z' fill='%23805500' fill-opacity='0.2' fill-rule='evenodd'/%3E%3C/svg%3E");
            }
        `}</style>
    </div>
);
};

export default RegistrationCashier;