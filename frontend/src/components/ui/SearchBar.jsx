import React from 'react';
import { useTranslation } from 'react-i18next';
import { Search } from 'lucide-react';

const SearchBar = ({ value, onChange, placeholder, className = '' }) => {
  const { t } = useTranslation();
  const resolvedPlaceholder = placeholder || t('common.search', 'Search...');

  return (
    <div className={`relative ${className}`}>
      <Search className="absolute left-3.5 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
      <input
        type="text"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        placeholder={resolvedPlaceholder}
        className="w-full pl-10 pr-4 py-2.5 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm transition-colors"
      />
    </div>
  );
};

export default SearchBar;
