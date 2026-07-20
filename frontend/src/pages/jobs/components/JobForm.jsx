import React, { useState, useEffect } from 'react';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import { Briefcase, Building2, MapPin, DollarSign, Calendar, AlignLeft, Users, SlidersHorizontal } from 'lucide-react';
import axiosClient from '../../../services/axiosClient';
import InputField from '../../../components/ui/InputField';
import SubmitButton from '../../../components/ui/SubmitButton';

const JobForm = ({ defaultValues = {}, onSubmit, loading, submitText, isEdit = false }) => {
  const { t } = useTranslation();
  const resolvedSubmitText = submitText || t('common.save', 'Save Job Posting');
  const [companies, setCompanies] = useState([]);

  useEffect(() => {
    const fetchCompanies = async () => {
      try {
        const response = await axiosClient.get('/companies', {
          params: { page: 0, size: 100 }
        });
        setCompanies(response.data.data.content || []);
      } catch (err) {
        console.error('Error fetching companies for form selector:', err);
      }
    };
    fetchCompanies();
  }, []);

  const getTodayDateString = () => {
    const today = new Date();
    const dd = String(today.getDate()).padStart(2, '0');
    const mm = String(today.getMonth() + 1).padStart(2, '0');
    const yyyy = today.getFullYear();
    return `${yyyy}-${mm}-${dd}`;
  };

  const {
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm({
    defaultValues: {
      title: defaultValues.title || '',
      companyId: defaultValues.company?.id || defaultValues.companyId || '',
      description: defaultValues.description || '',
      requirements: defaultValues.requirements || '',
      location: defaultValues.location || '',
      employmentType: defaultValues.employmentType || 'FULL_TIME',
      experienceLevel: defaultValues.experienceLevel || 'MID',
      salaryMin: defaultValues.salaryMin !== undefined ? defaultValues.salaryMin : '',
      salaryMax: defaultValues.salaryMax !== undefined ? defaultValues.salaryMax : '',
      deadline: defaultValues.deadline || '',
      status: defaultValues.status || 'OPEN',
    },
  });

  const watchSalaryMin = watch('salaryMin');

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Job Title */}
        <InputField
          label={t('jobs.jobTitle', 'Job Title')}
          id="title"
          icon={Briefcase}
          placeholder="e.g. Senior Java Backend Engineer"
          error={errors.title}
          {...register('title', {
            required: t('common.error', 'Job title is required'),
            maxLength: {
              value: 255,
              message: 'Title must not exceed 255 characters',
            },
          })}
        />

        {/* Company selection */}
        <div className="space-y-1.5">
          <label htmlFor="companyId" className="block text-sm font-semibold text-gray-700">
            {t('jobs.company', 'Target Company')}
          </label>
          <div className="relative rounded-md shadow-sm">
            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <Building2 className="h-5 w-5 text-gray-400" />
            </div>
            <select
              id="companyId"
              className={`block w-full pl-10 pr-4 py-2.5 border rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm bg-white text-gray-700 transition-colors ${
                errors.companyId ? 'border-red-300' : 'border-gray-300'
              }`}
              {...register('companyId', { required: t('common.error', 'Company is required') })}
            >
              <option value="">{t('jobs.allCompanies', 'Select Company')}</option>
              {companies.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.companyName || c.name}
                </option>
              ))}
            </select>
          </div>
          {errors.companyId && (
            <p className="text-xs text-red-600 font-semibold mt-1">{errors.companyId.message}</p>
          )}
        </div>

        {/* Location */}
        <InputField
          label={t('jobs.location', 'Location')}
          id="location"
          icon={MapPin}
          placeholder="e.g. San Francisco, CA (Hybrid)"
          error={errors.location}
          {...register('location', {
            maxLength: {
              value: 255,
              message: 'Location must not exceed 255 characters',
            },
          })}
        />

        {/* Employment Type */}
        <div className="space-y-1.5">
          <label htmlFor="employmentType" className="block text-sm font-semibold text-gray-700">
            {t('jobs.jobType', 'Employment Type')}
          </label>
          <div className="relative rounded-md shadow-sm">
            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <Briefcase className="h-5 w-5 text-gray-400" />
            </div>
            <select
              id="employmentType"
              className="block w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm bg-white text-gray-700"
              {...register('employmentType')}
            >
              <option value="FULL_TIME">Full-time</option>
              <option value="PART_TIME">Part-time</option>
              <option value="CONTRACT">Contract</option>
              <option value="FREELANCE">Freelance</option>
              <option value="INTERNSHIP">Internship</option>
            </select>
          </div>
        </div>

        {/* Experience Level */}
        <div className="space-y-1.5">
          <label htmlFor="experienceLevel" className="block text-sm font-semibold text-gray-700">
            {t('jobs.experienceLevel', 'Experience Level')}
          </label>
          <div className="relative rounded-md shadow-sm">
            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <Users className="h-5 w-5 text-gray-400" />
            </div>
            <select
              id="experienceLevel"
              className="block w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm bg-white text-gray-700"
              {...register('experienceLevel')}
            >
              <option value="ENTRY">Entry Level</option>
              <option value="JUNIOR">Junior</option>
              <option value="MID">Mid Level</option>
              <option value="SENIOR">Senior</option>
              <option value="LEAD">Lead</option>
              <option value="EXECUTIVE">Executive</option>
            </select>
          </div>
        </div>

        {/* Deadline */}
        <InputField
          label={t('jobs.deadline', 'Application Deadline')}
          id="deadline"
          type="date"
          icon={Calendar}
          error={errors.deadline}
          {...register('deadline', {
            validate: (value) => {
              if (!value) return true;
              const today = getTodayDateString();
              return value >= today || 'Deadline must be today or in the future';
            }
          })}
        />

        {/* Salary Min */}
        <InputField
          label={t('jobs.minSalary', 'Minimum Salary')}
          id="salaryMin"
          type="number"
          icon={DollarSign}
          placeholder="e.g. 80000"
          error={errors.salaryMin}
          {...register('salaryMin', {
            min: {
              value: 0,
              message: 'Salary must be a positive number',
            },
          })}
        />

        {/* Salary Max */}
        <InputField
          label={t('jobs.maxSalary', 'Maximum Salary')}
          id="salaryMax"
          type="number"
          icon={DollarSign}
          placeholder="e.g. 120000"
          error={errors.salaryMax}
          {...register('salaryMax', {
            min: {
              value: 0,
              message: 'Salary must be a positive number',
            },
            validate: (value) => {
              if (!value || !watchSalaryMin) return true;
              return parseFloat(value) >= parseFloat(watchSalaryMin) || 'Maximum salary must be greater than or equal to minimum salary';
            }
          })}
        />

        {/* Status (Only in edit mode) */}
        {isEdit && (
          <div className="space-y-1.5 md:col-span-2">
            <label htmlFor="status" className="block text-sm font-semibold text-gray-700">
              {t('jobs.status', 'Job Status')}
            </label>
            <div className="relative rounded-md shadow-sm">
              <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                <SlidersHorizontal className="h-5 w-5 text-gray-400" />
              </div>
              <select
                id="status"
                className="block w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm bg-white text-gray-700"
                {...register('status')}
              >
                <option value="OPEN">{t('status.ACTIVE', 'Open')}</option>
                <option value="DRAFT">{t('status.DRAFT', 'Draft')}</option>
                <option value="CLOSED">{t('status.CLOSED', 'Closed')}</option>
              </select>
            </div>
          </div>
        )}

        {/* Description */}
        <div className="md:col-span-2 space-y-1.5">
          <label htmlFor="description" className="block text-sm font-semibold text-gray-700">
            {t('jobs.description', 'Job Description')}
          </label>
          <div className="relative rounded-md shadow-sm">
            <div className="absolute inset-y-0 left-0 pl-3 pt-3 flex items-start pointer-events-none">
              <AlignLeft className="h-5 w-5 text-gray-400" />
            </div>
            <textarea
              id="description"
              rows={5}
              placeholder="Provide a description of the job duties..."
              className={`block w-full pl-10 pr-3.5 py-2.5 text-sm rounded-xl border focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors ${
                errors.description ? 'border-red-300 bg-red-50 text-red-900' : 'border-gray-300'
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

        {/* Requirements */}
        <div className="md:col-span-2 space-y-1.5">
          <label htmlFor="requirements" className="block text-sm font-semibold text-gray-700">
            {t('jobs.requirements', 'Qualifications & Requirements')}
          </label>
          <div className="relative rounded-md shadow-sm">
            <div className="absolute inset-y-0 left-0 pl-3 pt-3 flex items-start pointer-events-none">
              <AlignLeft className="h-5 w-5 text-gray-400" />
            </div>
            <textarea
              id="requirements"
              rows={5}
              placeholder="List the required skills, certifications, and experience..."
              className={`block w-full pl-10 pr-3.5 py-2.5 text-sm rounded-xl border focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors ${
                errors.requirements ? 'border-red-300 bg-red-50 text-red-900' : 'border-gray-300'
              }`}
              {...register('requirements', {
                maxLength: {
                  value: 5000,
                  message: 'Requirements must not exceed 5000 characters',
                },
              })}
            />
          </div>
          {errors.requirements && (
            <p className="text-xs text-red-600 font-semibold mt-1">{errors.requirements.message}</p>
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

export default JobForm;
