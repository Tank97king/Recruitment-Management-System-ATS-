import React from 'react';
import { useTranslation } from 'react-i18next';
import { Globe } from 'lucide-react';

const LanguageSwitcher = ({ className = '' }) => {
  const { i18n } = useTranslation();
  const currentLang = i18n.language || 'vi';

  const toggleLanguage = (lang) => {
    i18n.changeLanguage(lang);
  };

  return (
    <div className={`inline-flex items-center p-1 bg-gray-100 dark:bg-gray-800 rounded-xl border border-gray-200 text-xs font-semibold ${className}`}>
      <div className="flex items-center pl-2 pr-1 text-gray-500">
        <Globe className="w-4 h-4 mr-1 text-blue-600" />
      </div>
      <button
        type="button"
        onClick={() => toggleLanguage('vi')}
        className={`px-2.5 py-1 rounded-lg transition-all duration-150 flex items-center gap-1 ${
          currentLang === 'vi'
            ? 'bg-white text-blue-700 shadow-sm font-bold'
            : 'text-gray-600 hover:text-gray-900 hover:bg-gray-200/60'
        }`}
      >
        <span>🇻🇳</span>
        <span>VI</span>
      </button>
      <button
        type="button"
        onClick={() => toggleLanguage('en')}
        className={`px-2.5 py-1 rounded-lg transition-all duration-150 flex items-center gap-1 ${
          currentLang === 'en'
            ? 'bg-white text-blue-700 shadow-sm font-bold'
            : 'text-gray-600 hover:text-gray-900 hover:bg-gray-200/60'
        }`}
      >
        <span>🇺🇸</span>
        <span>EN</span>
      </button>
    </div>
  );
};

export default LanguageSwitcher;
