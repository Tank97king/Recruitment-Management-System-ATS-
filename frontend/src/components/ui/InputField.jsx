import React from 'react';

const InputField = React.forwardRef(({ label, id, error, icon: Icon, ...props }, ref) => {
  return (
    <div className="space-y-1.5">
      {label && (
        <label htmlFor={id} className="block text-sm font-semibold text-gray-700">
          {label}
        </label>
      )}
      <div className="relative rounded-md shadow-sm">
        {Icon && (
          <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
            <Icon className="h-5 w-5 text-gray-400" />
          </div>
        )}
        <input
          id={id}
          ref={ref}
          className={`block w-full text-sm rounded-xl border focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors ${
            Icon ? 'pl-10' : 'px-3.5'
          } py-2.5 ${
            error ? 'border-red-300 bg-red-50 text-red-900 focus:ring-red-500 focus:border-red-500' : 'border-gray-300'
          }`}
          {...props}
        />
      </div>
      {error && (
        <p className="text-xs text-red-600 font-semibold mt-1">{error.message || error}</p>
      )}
    </div>
  );
});

InputField.displayName = 'InputField';

export default InputField;
