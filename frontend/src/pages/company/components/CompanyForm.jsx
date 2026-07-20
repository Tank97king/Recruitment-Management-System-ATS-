import React from 'react';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import { Building2, Mail, Phone, Globe, MapPin, AlignLeft } from 'lucide-react';
import InputField from '../../../components/ui/InputField';
import SubmitButton from '../../../components/ui/SubmitButton';

const CompanyForm = ({ defaultValues = {}, onSubmit, loading, submitText }) => {
  const { t } = useTranslation();
  const resolvedSubmitText = submitText || t('common.save', 'Save Company');

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    defaultValues: {
      companyName: defaultValues.companyName || '',
      email: defaultValues.email || '',
      phone: defaultValues.phone || '',
      website: defaultValues.website || '',
      address: defaultValues.address || '',
      description: defaultValues.description || '',
    },
  });

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Name */}
        <InputField
          label={t('companies.companyName', 'Company Name')}
          id="companyName"
          icon={Building2}
          placeholder="TechSoft Solutions Inc."
          error={errors.companyName}
          {...register('companyName', {
            required: t('common.error', 'Company name is required'),
            maxLength: {
              value: 255,
              message: 'Company name must not exceed 255 characters',
            },
          })}
        />

        {/* Email */}
        <InputField
          label={t('candidates.email', 'Email Address')}
          id="email"
          type="email"
          icon={Mail}
          placeholder="hr@techsoft.com"
          error={errors.email}
          {...register('email', {
            maxLength: {
              value: 255,
              message: 'Email must not exceed 255 characters',
            },
            pattern: {
              value: /^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$/i,
              message: 'Invalid email address format',
            },
          })}
        />

        {/* Phone */}
        <InputField
          label={t('candidates.phone', 'Phone Number')}
          id="phone"
          icon={Phone}
          placeholder="+1 (555) 123-4567"
          error={errors.phone}
          {...register('phone', {
            maxLength: {
              value: 50,
              message: 'Phone number must not exceed 50 characters',
            },
          })}
        />

        {/* Website */}
        <InputField
          label={t('companies.website', 'Website URL')}
          id="website"
          icon={Globe}
          placeholder="https://techsoft.example.com"
          error={errors.website}
          {...register('website', {
            maxLength: {
              value: 500,
              message: 'Website URL must not exceed 500 characters',
            },
          })}
        />

        {/* Address */}
        <div className="md:col-span-2">
          <InputField
            label={t('companies.location', 'Headquarters Address')}
            id="address"
            icon={MapPin}
            placeholder="123 Tech Boulevard, Austin, TX 78701"
            error={errors.address}
            {...register('address', {
              maxLength: {
                value: 500,
                message: 'Address must not exceed 500 characters',
              },
            })}
          />
        </div>

        {/* Description */}
        <div className="md:col-span-2 space-y-1.5">
          <label htmlFor="description" className="block text-sm font-semibold text-gray-700">
            {t('companies.description', 'Company Description')}
          </label>
          <div className="relative rounded-md shadow-sm">
            <div className="absolute inset-y-0 left-0 pl-3 pt-3 flex items-start pointer-events-none">
              <AlignLeft className="h-5 w-5 text-gray-400" />
            </div>
            <textarea
              id="description"
              rows={5}
              placeholder="Provide a detailed description of the company..."
              className={`block w-full pl-10 pr-3.5 py-2.5 text-sm rounded-xl border focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors ${
                errors.description ? 'border-red-300 bg-red-50 text-red-900 focus:ring-red-500 focus:border-red-500' : 'border-gray-300'
              }`}
              {...register('description', {
                maxLength: {
                  value: 5000,
                  message: 'Description must not exceed 5000 characters',
                },
              })}
            />
          </div>
          {errors.description && (
            <p className="text-xs text-red-600 font-semibold mt-1">{errors.description.message}</p>
          )}
        </div>
      </div>

      <div className="flex justify-end gap-3 pt-4 border-t border-gray-100">
        <SubmitButton loading={loading} className="w-auto px-6">
          {resolvedSubmitText}
        </SubmitButton>
      </div>
    </form>
  );
};

export default CompanyForm;
