import React, { useState, useEffect, useRef } from 'react';
import { Search, ChevronDown, X, Loader2 } from 'lucide-react';

const SearchableSelect = ({ 
  label, 
  placeholder = 'Search & select...', 
  options = [], 
  value, 
  onChange, 
  error,
  loading = false,
  noOptionsMessage = 'No options found'
}) => {
  const [isOpen, setIsOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const containerRef = useRef(null);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (containerRef.current && !containerRef.current.contains(event.target)) {
        setIsOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const selectedOption = options.find(opt => opt.value === value);

  const filteredOptions = options.filter(opt => 
    opt.label.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const handleSelect = (val) => {
    onChange(val);
    setIsOpen(false);
    setSearchTerm('');
  };

  const handleClear = (e) => {
    e.stopPropagation();
    onChange('');
    setSearchTerm('');
  };

  return (
    <div ref={containerRef} className="space-y-1.5 relative w-full">
      {label && <label className="block text-sm font-semibold text-gray-700">{label}</label>}
      
      <div 
        onClick={() => !loading && setIsOpen(prev => !prev)}
        className={`flex items-center justify-between w-full pl-3.5 pr-3 py-2.5 border rounded-xl bg-white text-sm cursor-pointer transition-colors ${
          error ? 'border-red-300 bg-red-50 text-red-900 focus:ring-red-500' : 'border-gray-300 hover:border-gray-400 focus-within:ring-2 focus-within:ring-blue-500'
        }`}
      >
        <div className="flex-1 truncate pr-2">
          {selectedOption ? (
            <span className="text-gray-800 font-bold">{selectedOption.label}</span>
          ) : (
            <span className="text-gray-400 font-semibold">{placeholder}</span>
          )}
        </div>
        <div className="flex items-center gap-1.5 flex-shrink-0">
          {loading && <Loader2 className="w-4 h-4 animate-spin text-gray-400" />}
          {selectedOption && !loading && (
            <button 
              type="button" 
              onClick={handleClear} 
              className="p-0.5 text-gray-400 hover:text-gray-600 rounded-md"
            >
              <X className="w-4 h-4" />
            </button>
          )}
          <ChevronDown className={`w-4 h-4 text-gray-400 transition-transform ${isOpen ? 'rotate-180' : ''}`} />
        </div>
      </div>

      {isOpen && (
        <div className="absolute z-30 w-full mt-1.5 bg-white border border-gray-200 rounded-xl shadow-lg max-h-60 overflow-hidden flex flex-col">
          <div className="p-2 border-b border-gray-100 flex items-center relative">
            <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
            <input
              type="text"
              autoFocus
              placeholder="Search..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pl-8 pr-3 py-1.5 border border-gray-205 rounded-lg text-xs font-semibold focus:outline-none focus:ring-2 focus:ring-blue-500 bg-gray-50/50"
            />
          </div>
          <div className="overflow-y-auto flex-1 py-1">
            {filteredOptions.length > 0 ? (
              filteredOptions.map((opt) => (
                <div
                  key={opt.value}
                  onClick={() => handleSelect(opt.value)}
                  className={`px-4 py-2 text-xs font-bold text-gray-700 hover:bg-blue-50 hover:text-blue-700 cursor-pointer ${
                    opt.value === value ? 'bg-blue-50/50 text-blue-600' : ''
                  }`}
                >
                  {opt.label}
                  {opt.sublabel && (
                    <span className="block text-[10px] text-gray-400 font-semibold mt-0.5">{opt.sublabel}</span>
                  )}
                </div>
              ))
            ) : (
              <div className="px-4 py-3 text-xs font-semibold text-gray-400 italic text-center">
                {noOptionsMessage}
              </div>
            )}
          </div>
        </div>
      )}

      {error && (
        <p className="text-xs text-red-600 font-semibold mt-1">{error.message}</p>
      )}
    </div>
  );
};

export default SearchableSelect;
