import React from 'react';
import { useTranslation } from 'react-i18next';
import { Briefcase } from 'lucide-react';
import AuthCard from '../../components/ui/AuthCard';
import LoginForm from './components/LoginForm';
import LanguageSwitcher from '../../components/common/LanguageSwitcher';

const Login = () => {
  const { t } = useTranslation();

  return (
    <div className="min-h-screen bg-gray-50 flex flex-col justify-center py-12 sm:px-6 lg:px-8 animate-fadeIn relative">
      <div className="absolute top-4 right-4 sm:top-6 sm:right-6">
        <LanguageSwitcher />
      </div>

      <div className="sm:mx-auto sm:w-full sm:max-w-md">
        <div className="flex justify-center">
          <div className="w-12 h-12 rounded-xl bg-blue-600 flex items-center justify-center shadow-lg shadow-blue-200">
            <Briefcase className="w-6 h-6 text-white" />
          </div>
        </div>
        <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900 tracking-tight">
          {t('auth.login', 'Sign In')}
        </h2>
        <p className="mt-2 text-center text-sm text-gray-600">
          {t('auth.loginSubtitle', 'Sign in to access your recruitment management portal')}
        </p>
      </div>

      <div className="mt-8 sm:mx-auto sm:w-full sm:max-w-md">
        <AuthCard>
          <LoginForm />
          <div className="mt-6 text-center text-xs text-gray-400">
            Default Admin: admin@example.com / password
          </div>
        </AuthCard>
      </div>
    </div>
  );
};

export default Login;
