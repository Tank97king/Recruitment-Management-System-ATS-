import React, { useState } from 'react';
import { useForm } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import { User, Mail, Phone, Calendar, Users, MapPin, Briefcase, GraduationCap, X, AlignLeft } from 'lucide-react';
import InputField from '../../../components/ui/InputField';
import SubmitButton from '../../../components/ui/SubmitButton';

const CandidateForm = ({ defaultValues = {}, onSubmit, loading, submitText }) => {
  const { t } = useTranslation();
  const resolvedSubmitText = submitText || t('common.save', 'Save Candidate Profile');
  const [skills, setSkills] = useState(defaultValues.skills || []);
  const [skillInput, setSkillInput] = useState('');

  const getMaxDobString = () => {
    const today = new Date();
    const dd = String(today.getDate()).padStart(2, '0');
    const mm = String(today.getMonth() + 1).padStart(2, '0');
    const yyyy = today.getFullYear() - 1;
    return `${yyyy}-${mm}-${dd}`;
  };

  const {
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    defaultValues: {
      fullName: defaultValues.fullName || '',
      email: defaultValues.email || '',
      phone: defaultValues.phone || '',
      dateOfBirth: defaultValues.dateOfBirth || '',
      gender: defaultValues.gender || '',
      address: defaultValues.address || '',
      yearsOfExperience: defaultValues.yearsOfExperience !== undefined ? defaultValues.yearsOfExperience : '',
      highestEducation: defaultValues.highestEducation || '',
      summary: defaultValues.summary || '',
    },
  });

  const handleAddSkill = (e) => {
    if (e.key === 'Enter' || e.key === ',') {
      e.preventDefault();
      const val = skillInput.trim().replace(',', '');
      if (val && !skills.includes(val)) {
        setSkills([...skills, val]);
        setSkillInput('');
      }
    }
  };

  const handleRemoveSkill = (index) => {
    setSkills(skills.filter((_, idx) => idx !== index));
  };

  const handleFormSubmit = (data) => {
    onSubmit({
      ...data,
      skills,
    });
  };

  return (
    <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Full Name */}
        <InputField
          label={t('candidates.name', 'Full Name')}
          id="fullName"
          icon={User}
          placeholder="e.g. Jane Smith"
          error={errors.fullName}
          {...register('fullName', {
            required: t('common.error', 'Full name is required'),
            maxLength: {
              value: 200,
              message: 'Full name must not exceed 200 characters',
            },
          })}
        />

        {/* Email */}
        <InputField
          label={t('candidates.email', 'Email Address')}
          id="email"
          type="email"
          icon={Mail}
          placeholder="e.g. jane.smith@email.com"
          error={errors.email}
          {...register('email', {
            required: t('common.error', 'Email address is required'),
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
          placeholder="e.g. +1 (555) 987-6543"
          error={errors.phone}
          {...register('phone', {
            maxLength: {
              value: 50,
              message: 'Phone number must not exceed 50 characters',
            },
          })}
        />

        {/* Date of Birth */}
        <InputField
          label={t('candidates.name', 'Date of Birth')}
          id="dateOfBirth"
          type="date"
          icon={Calendar}
          error={errors.dateOfBirth}
          max={getMaxDobString()}
          {...register('dateOfBirth', {
            validate: (value) => {
              if (!value) return true;
              const maxDate = getMaxDobString();
              return value <= maxDate || 'Date of birth must be a past date';
            }
          })}
        />

        {/* Gender */}
        <div className="space-y-1.5">
          <label htmlFor="gender" className="block text-sm font-semibold text-gray-700">
            Gender
          </label>
          <div className="relative rounded-md shadow-sm">
            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <Users className="h-5 w-5 text-gray-400" />
            </div>
            <select
              id="gender"
              className="block w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm bg-white text-gray-700"
              {...register('gender', {
                maxLength: {
                  value: 50,
                  message: 'Gender must not exceed 50 characters',
                },
              })}
            >
              <option value="">Select Gender</option>
              <option value="Male">Male</option>
              <option value="Female">Female</option>
              <option value="Other">Other</option>
              <option value="Decline">Prefer not to say</option>
            </select>
          </div>
        </div>

        {/* Experience */}
        <InputField
          label={t('candidates.experienceYears', 'Years of Experience')}
          id="yearsOfExperience"
          type="number"
          icon={Briefcase}
          placeholder="e.g. 5"
          error={errors.yearsOfExperience}
          {...register('yearsOfExperience', {
            min: {
              value: 0,
              message: 'Experience must be 0 or positive',
            },
          })}
        />

        {/* Education */}
        <InputField
          label={t('candidates.education', 'Highest Education')}
          id="highestEducation"
          icon={GraduationCap}
          placeholder="e.g. Master of Science - MIT (2018)"
          error={errors.highestEducation}
          {...register('highestEducation', {
            maxLength: {
              value: 255,
              message: 'Highest education must not exceed 255 characters',
            },
          })}
        />

        {/* Address */}
        <InputField
          label={t('companies.location', 'Residential Address')}
          id="address"
          icon={MapPin}
          placeholder="e.g. 456 Oak Avenue, Austin, TX 78701"
          error={errors.address}
          {...register('address', {
            maxLength: {
              value: 255,
              message: 'Address must not exceed 255 characters',
            },
          })}
        />

        {/* Skills Tag Compiler */}
        <div className="md:col-span-2 space-y-1.5">
          <label className="block text-sm font-semibold text-gray-700">
            {t('candidates.skills', 'Skills & Keyword Tags')}
          </label>
          <div className="relative rounded-md shadow-sm">
            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <Briefcase className="h-5 w-5 text-gray-400" />
            </div>
            <input
              type="text"
              value={skillInput}
              onChange={(e) => setSkillInput(e.target.value)}
              onKeyDown={handleAddSkill}
              placeholder="Type a skill and press Enter or comma..."
              className="block w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm"
            />
          </div>
          {skills.length > 0 && (
            <div className="flex flex-wrap gap-2 pt-2">
              {skills.map((skill, index) => (
                <span 
                  key={index} 
                  className="inline-flex items-center gap-1 px-3 py-1 bg-blue-50 text-blue-700 font-bold border border-blue-100 rounded-xl text-xs"
                >
                  {skill}
                  <button 
                    type="button" 
                    onClick={() => handleRemoveSkill(index)}
                    className="hover:text-blue-900 focus:outline-none"
                  >
                    <X className="w-3.5 h-3.5" />
                  </button>
                </span>
              ))}
            </div>
          )}
        </div>

        {/* Summary */}
        <div className="md:col-span-2 space-y-1.5">
          <label htmlFor="summary" className="block text-sm font-semibold text-gray-700">
            {t('jobs.description', 'Professional Summary')}
          </label>
          <div className="relative rounded-md shadow-sm">
            <div className="absolute inset-y-0 left-0 pl-3 pt-3 flex items-start pointer-events-none">
              <AlignLeft className="h-5 w-5 text-gray-400" />
            </div>
            <textarea
              id="summary"
              rows={5}
              placeholder="Provide a brief summary of the candidate's professional background..."
              className={`block w-full pl-10 pr-3.5 py-2.5 text-sm rounded-xl border focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors ${
                errors.summary ? 'border-red-300 bg-red-50 text-red-900' : 'border-gray-300'
              }`}
              {...register('summary', {
                maxLength: {
                  value: 5000,
                  message: 'Summary must not exceed 5000 characters',
                },
              })}
            />
          </div>
          {errors.summary && (
            <p className="text-xs text-red-600 font-semibold mt-1">{errors.summary.message}</p>
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

export default CandidateForm;
