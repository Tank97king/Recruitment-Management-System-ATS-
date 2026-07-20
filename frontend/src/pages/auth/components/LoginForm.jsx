import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useNavigate, useLocation } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Mail } from 'lucide-react';
import { toast } from 'react-hot-toast';
import { useAuth } from '../../../context/AuthContext';
import InputField from '../../../components/ui/InputField';
import PasswordField from '../../../components/ui/PasswordField';
import SubmitButton from '../../../components/ui/SubmitButton';

const LoginForm = () => {
  const { t } = useTranslation();
  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [loading, setLoading] = useState(false);

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    defaultValues: {
      email: '',
      password: '',
    },
  });

  const from = location.state?.from?.pathname || '/dashboard';

  const onSubmit = async (data) => {
    setLoading(true);
    const result = await login(data.email, data.password);
    setLoading(false);

    if (result.success) {
      toast.success(`${t('common.success')}: Welcome back, ${result.user.fullName}!`);
      navigate(from, { replace: true });
    } else {
      let userFriendlyMsg = result.message;
      if (result.message === 'Network Error' || result.message?.includes('ERR_CONNECTION_REFUSED')) {
        userFriendlyMsg = t('common.error') + ': Network error: Backend server is unreachable.';
      } else if (result.message?.includes('400')) {
        userFriendlyMsg = 'Invalid email format or password requirements not met.';
      } else if (result.message?.includes('401') || result.message?.includes('403')) {
        userFriendlyMsg = 'Invalid email or password. Please try again.';
      }
      toast.error(userFriendlyMsg);
    }
  };

  return (
    <form className="space-y-6" onSubmit={handleSubmit(onSubmit)}>
      {/* Email Field */}
      <InputField
        label={t('auth.email', 'Email Address')}
        id="email"
        type="email"
        icon={Mail}
        placeholder="recruiter@example.com"
        error={errors.email}
        {...register('email', {
          required: 'Email is required',
          pattern: {
            value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
            message: 'Invalid email address format',
          },
        })}
      />

      {/* Password Field */}
      <PasswordField
        label={t('auth.password', 'Password')}
        id="password"
        placeholder="••••••••"
        error={errors.password}
        {...register('password', {
          required: 'Password is required',
          minLength: {
            value: 6,
            message: 'Password must be at least 6 characters long',
          },
        })}
      />

      <div className="flex items-center justify-between">
        <div className="flex items-center">
          <input
            id="remember-me"
            name="remember-me"
            type="checkbox"
            className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded cursor-pointer"
          />
          <label htmlFor="remember-me" className="ml-2 block text-xs text-gray-500 font-medium cursor-pointer select-none">
            {t('auth.rememberMe', 'Remember me')}
          </label>
        </div>

        <div>
          <span 
            className="text-xs font-semibold text-gray-400 cursor-not-allowed select-none"
            title="Forgot Password is coming soon"
          >
            {t('auth.forgotPassword', 'Forgot password?')}
          </span>
        </div>
      </div>

      <div>
        <SubmitButton loading={loading}>
          {loading ? t('auth.signingIn', 'Signing in...') : t('auth.login', 'Sign In')}
        </SubmitButton>
      </div>
    </form>
  );
};

export default LoginForm;
