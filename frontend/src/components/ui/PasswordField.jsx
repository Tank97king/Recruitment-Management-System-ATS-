import React, { useState } from 'react';
import { Lock, Eye, EyeOff } from 'lucide-react';

const PasswordField = React.forwardRef(({ label, id, error, ...props }, ref) => {
  const [showPassword, setShowPassword] = useState(false);

  return (
    <div className="space-y-1.5">
      {label && (
        <label htmlFor={id} className="block text-sm font-semibold text-gray-700">
          {label}
        </label>
      )}
      <div className="relative rounded-md shadow-sm">
        <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
          <Lock className="h-5 w-5 text-gray-400" />
        </div>
        <input
          id={id}
          type={showPassword ? 'text' : 'password'}
          ref={ref}
          className={`block w-full pl-10 pr-10 py-2.5 border rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm transition-colors ${
            error ? 'border-red-300 bg-red-50 text-red-900 focus:ring-red-500 focus:border-red-500' : 'border-gray-300'
          }`}
          {...props}
        />
        <button
          type="button"
          onClick={() => setShowPassword(!showPassword)}
          className="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-400 hover:text-gray-600"
        >
          {showPassword ? <EyeOff className="h-5 w-5" /> : <Eye className="h-5 w-5" />}
        </button>
      </div>
      {error && (
        <p className="text-xs text-red-600 font-semibold mt-1">{error.message || error}</p>
      )}
    </div>
  );
});

PasswordField.displayName = 'PasswordField';

export default PasswordField;
