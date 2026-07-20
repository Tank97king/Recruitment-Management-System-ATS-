import React from 'react';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import { User, Phone, Mail, Shield } from 'lucide-react';
import InputField from '../../../components/ui/InputField';
import SubmitButton from '../../../components/ui/SubmitButton';

const ProfileForm = ({ user = {}, onSubmit, loading }) => {
  const { t } = useTranslation();
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    defaultValues: {
      fullName: user.fullName || '',
      phone: user.phone || '',
    },
  });

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
      <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
        {/* Full Name */}
        <InputField
          label={t('auth.fullName', 'Full Name')}
          id="fullName"
          icon={User}
          placeholder="e.g. John Doe"
          error={errors.fullName}
          {...register('fullName', {
            required: t('common.error', 'Full name is required'),
            maxLength: {
              value: 100,
              message: 'Full name must not exceed 100 characters',
            },
          })}
        />

        {/* Phone number */}
        <InputField
          label={t('candidates.phone', 'Phone Number')}
          id="phone"
          icon={Phone}
          placeholder="e.g. +1 555-123-4567"
          error={errors.phone}
          {...register('phone', {
            maxLength: {
              value: 20,
              message: 'Phone number must not exceed 20 characters',
            },
            pattern: {
              value: /^[+]?[0-9\s\-()]{7,20}$/,
              message: 'Invalid phone number format',
            },
          })}
        />

        {/* Read-only fields */}
        <div className="space-y-1.5">
          <label className="block text-xs font-bold text-gray-450 uppercase tracking-wider">{t('auth.email', 'Email Address')} ({t('status.inactive', 'Read-only')})</label>
          <div className="relative rounded-xl shadow-sm">
            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <Mail className="h-5 w-5 text-gray-400" />
            </div>
            <input
              type="text"
              value={user.email || ''}
              disabled
              className="block w-full pl-10 pr-4 py-2.5 border border-gray-200 rounded-xl bg-gray-50 text-gray-400 text-xs font-semibold select-none cursor-not-allowed"
            />
          </div>
        </div>

        <div className="space-y-1.5">
          <label className="block text-xs font-bold text-gray-400 uppercase tracking-wider">{t('profile.accountRole', 'Security Role')} ({t('status.inactive', 'Read-only')})</label>
          <div className="relative rounded-xl shadow-sm">
            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <Shield className="h-5 w-5 text-gray-400" />
            </div>
            <input
              type="text"
              value={user.role || 'RECRUITER'}
              disabled
              className="block w-full pl-10 pr-4 py-2.5 border border-gray-200 rounded-xl bg-gray-50 text-gray-400 text-xs font-semibold select-none cursor-not-allowed uppercase"
            />
          </div>
        </div>
      </div>

      <div className="flex justify-end pt-4 border-t border-gray-100">
        <SubmitButton loading={loading} className="w-auto px-6">
          {t('common.save', 'Save Settings Profile')}
        </SubmitButton>
      </div>
    </form>
  );
};

export default ProfileForm;
