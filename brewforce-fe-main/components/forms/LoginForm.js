'use client';

import React, { useState } from "react";
import { useRouter } from 'next/navigation';
import { jwtDecode } from 'jwt-decode';
import validator from 'validator';


const LoginForm = () => {
  const router = useRouter();
  const [formData, setFormData] = useState({
    username: "",
    password: "",
  });
  const [error, setError] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prevState) => ({ ...prevState, [name]: value }));
    if (error) setError("");
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);
    setError("");

    const sanitizedData = {
      username: validator.trim(validator.escape(formData.username)),
      password: formData.password,
    };

    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_URL_AUTH}/api/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(sanitizedData),
      });
      
      const data = await response.json();
      
      if (response.ok) {
        localStorage.setItem('token', data.data.token);
        router.push('/');
      } else {
        setError(data.message || "Login failed");
      }
    } catch (error) {
      setError("Error connecting to server");
      console.error("Login error:", error);
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="bg-white rounded-lg shadow-xl p-8 border border-amber-200">
      <h1 className="text-3xl font-bold text-amber-900 mb-6">Welcome Back</h1>
      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Username Field */}
        <div>
          <label htmlFor="username" className="block text-sm font-semibold text-gray-800 mb-2"> {/* Changed to gray-800 */}
            Username
          </label>
          <input
            type="text"
            id="username"
            name="username"
            className="w-full px-4 py-3 rounded-md border border-amber-300 focus:outline-none focus:ring-2 focus:ring-amber-500 focus:border-amber-500 text-gray-800 placeholder-gray-500"
            placeholder="Enter your username"
            value={formData.username}
            onChange={handleChange}
            required
          />
        </div>

        {/* Password Field */}
        <div>
          <label htmlFor="password" className="block text-sm font-semibold text-gray-800 mb-2"> {/* Changed to gray-800 */}
            Password
          </label>
          <div className="relative">
            <input
              type={showPassword ? "text" : "password"}
              id="password"
              name="password"
              className="w-full px-4 py-3 rounded-md border border-amber-300 focus:outline-none focus:ring-2 focus:ring-amber-500 focus:border-amber-500 text-gray-800 placeholder-gray-500"
              placeholder="Enter your password"
              value={formData.password}
              onChange={handleChange}
              required
            />
            <button
              type="button"
              className="absolute inset-y-0 right-0 flex items-center pr-3"
              onClick={() => setShowPassword(!showPassword)}
              aria-label={showPassword ? "Hide password" : "Show password"}
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
          <div className="bg-red-100 border-l-4 border-red-500 text-red-700 p-4 rounded-md">
            <p className="font-medium">{error}</p>
          </div>
        )}

        <button
          type="submit"
          disabled={isSubmitting}
          className={`w-full py-3 px-4 bg-amber-800 text-white rounded-md transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-amber-500 ${
            isSubmitting ? 'opacity-75 cursor-not-allowed' : 'hover:bg-amber-700'
          }`}
        >
          {isSubmitting ? 'Logging In...' : 'Login'}
        </button>

        {/* Registration Link */}
        <div className="text-center mt-4">
          <p className="text-amber-800"> 
            Don't have an account?{' '}
            <a 
              href="/register" 
              className="font-semibold text-amber-700 hover:text-amber-800 underline transition-colors" 
            >
              Register here
            </a>
          </p>
        </div>
      </form>
    </div>
  );
};

export default LoginForm;