import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import viTranslation from './locales/vi.json';
import enTranslation from './locales/en.json';

const savedLanguage = localStorage.getItem('app_lang') || 'vi';

i18n
  .use(initReactI18next)
  .init({
    resources: {
      vi: { translation: viTranslation },
      en: { translation: enTranslation }
    },
    lng: savedLanguage,
    fallbackLng: 'vi',
    interpolation: {
      escapeValue: false
    }
  });

i18n.on('languageChanged', (lng) => {
  localStorage.setItem('app_lang', lng);
});

export default i18n;
