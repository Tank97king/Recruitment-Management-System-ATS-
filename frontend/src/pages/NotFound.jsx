import React from 'react';
import { Link } from 'react-router-dom';
import { Briefcase, AlertCircle, ArrowLeft } from 'lucide-react';

const NotFound = () => {
  return (
    <div className="min-h-screen bg-gray-50 flex flex-col justify-center items-center py-12 px-4 sm:px-6 lg:px-8 animate-fadeIn">
      <div className="max-w-md w-full text-center space-y-6">
        <div className="flex justify-center">
          <div className="w-16 h-16 rounded-2xl bg-blue-50 text-blue-600 border border-blue-100 flex items-center justify-center shadow-md shadow-blue-50">
            <AlertCircle className="w-8 h-8" />
          </div>
        </div>
        
        <div className="space-y-2">
          <h1 className="text-6xl font-extrabold text-blue-600 tracking-tight">404</h1>
          <h2 className="text-2xl font-bold text-gray-800">Page Not Found</h2>
          <p className="text-sm text-gray-500 max-w-xs mx-auto">
            Sorry, we couldn't find the page you are looking for. It might have been moved or deleted.
          </p>
        </div>

        <div className="pt-4">
          <Link
            to="/dashboard"
            className="inline-flex items-center justify-center px-5 py-3 border border-transparent text-sm font-semibold rounded-xl text-white bg-blue-600 hover:bg-blue-755 shadow-md shadow-blue-100 transition-colors gap-2"
          >
            <ArrowLeft className="w-4 h-4" /> Back to Dashboard
          </Link>
        </div>
      </div>
    </div>
  );
};

export default NotFound;
