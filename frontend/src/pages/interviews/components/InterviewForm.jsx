import React, { useState, useEffect } from 'react';
import { useForm, Controller } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import { Calendar, User, Mail, Link as LinkIcon, MapPin, AlignLeft, Briefcase } from 'lucide-react';
import axiosClient from '../../../services/axiosClient';
import SearchableSelect from '../../applications/components/SearchableSelect';
import InputField from '../../../components/ui/InputField';
import SubmitButton from '../../../components/ui/SubmitButton';

const InterviewForm = ({ defaultValues = {}, onSubmit, loading, submitText, isEdit = false }) => {
  const { t } = useTranslation();
  const resolvedSubmitText = submitText || t('interviews.scheduleInterview', 'Schedule Interview');
  const [applications, setApplications] = useState([]);
  const [fetchingApps, setFetchingApps] = useState(true);

  useEffect(() => {
    if (isEdit) return;

    const fetchApplications = async () => {
      setFetchingApps(true);
      try {
        const response = await axiosClient.get('/job-applications', {
          params: { page: 0, size: 500, sortBy: 'appliedAt', sortDirection: 'desc' }
        });
        
        const filteredApps = (response.data.data.content || [])
          .filter(app => app.applicationStatus === 'INTERVIEW')
          .map(app => ({
            value: app.id,
            label: `${app.candidateName} - ${app.jobTitle}`,
            sublabel: `${app.companyName}`
          }));

        setApplications(filteredApps);
      } catch (err) {
        console.error('Error fetching applications for interview scheduler:', err);
      } finally {
        setFetchingApps(false);
      }
    };
    fetchApplications();
  }, [isEdit]);

  const formatDateTimeLocal = (dateStr) => {
    if (!dateStr) return '';
    return dateStr.substring(0, 16);
  };

  const getMinDateTimeString = () => {
    const now = new Date();
    now.setMinutes(now.getMinutes() + 1);
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day}T${hours}:${minutes}`;
  };

  const {
    control,
    register,
    handleSubmit,
    watch,
    formState: { errors },
  } = useForm({
    defaultValues: {
      jobApplicationId: defaultValues.jobApplicationId || '',
      interviewDate: formatDateTimeLocal(defaultValues.interviewDate),
      interviewType: defaultValues.interviewType || 'ONLINE',
      interviewerName: defaultValues.interviewerName || '',
      interviewerEmail: defaultValues.interviewerEmail || '',
      meetingLocation: defaultValues.meetingLocation || '',
      meetingLink: defaultValues.meetingLink || '',
      notes: defaultValues.notes || '',
    },
  });

  const watchInterviewType = watch('interviewType');

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {!isEdit ? (
          <div className="md:col-span-2">
            <Controller
              name="jobApplicationId"
              control={control}
              rules={{ required: t('common.error', 'Job application is required') }}
              render={({ field }) => (
                <SearchableSelect
                  label={t('interviews.application', 'Job Application')}
                  placeholder={t('interviews.application', 'Select application under interview...')}
                  options={applications}
                  value={field.value}
                  onChange={field.onChange}
                  error={errors.jobApplicationId}
                  loading={fetchingApps}
                  noOptionsMessage={
                    fetchingApps 
                      ? t('common.loading', 'Loading applications...') 
                      : t('common.noData', 'No applications in INTERVIEW status found')
                  }
                />
              )}
            />
          </div>
        ) : (
          <div className="md:col-span-2 space-y-1 bg-gray-50 p-4 border border-gray-200 rounded-xl">
            <span className="block text-[10px] font-bold text-gray-400 uppercase tracking-wider">{t('interviews.application', 'Candidate & Opening')}</span>
            <p className="text-sm font-bold text-gray-700">
              {defaultValues.candidateName} — {defaultValues.jobTitle}
            </p>
            <p className="text-xs text-gray-450 font-semibold">{defaultValues.companyName}</p>
          </div>
        )}

        {/* Date Time picker */}
        <InputField
          label={t('interviews.dateTime', 'Interview Date & Time')}
          id="interviewDate"
          type="datetime-local"
          icon={Calendar}
          error={errors.interviewDate}
          min={getMinDateTimeString()}
          {...register('interviewDate', {
            required: t('common.error', 'Interview date and time is required'),
            validate: (value) => {
              if (!value) return true;
              const inputTime = new Date(value).getTime();
              const nowTime = new Date().getTime();
              return inputTime > nowTime || 'Interview date and time must be in the future';
            }
          })}
        />

        {/* Mode / Type dropdown */}
        <div className="space-y-1.5">
          <label htmlFor="interviewType" className="block text-sm font-semibold text-gray-700">
            {t('interviews.interviewType', 'Interview Mode')}
          </label>
          <div className="relative rounded-md shadow-sm">
            <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
              <Briefcase className="h-5 w-5 text-gray-400" />
            </div>
            <select
              id="interviewType"
              className="block w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-xl focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm bg-white text-gray-700"
              {...register('interviewType')}
            >
              <option value="ONLINE">{t('interviews.typeVideo', 'Online (VIDEO_CALL)')}</option>
              <option value="OFFLINE">{t('interviews.typeOnsite', 'Offline (IN_PERSON)')}</option>
            </select>
          </div>
        </div>

        {/* Interviewer Name */}
        <InputField
          label={t('interviews.interviewer', 'Interviewer Full Name')}
          id="interviewerName"
          icon={User}
          placeholder="e.g. Michael Johnson"
          error={errors.interviewerName}
          {...register('interviewerName', {
            required: t('common.error', 'Interviewer name is required'),
            maxLength: {
              value: 255,
              message: 'Name must not exceed 255 characters',
            },
          })}
        />

        {/* Interviewer Email */}
        <InputField
          label={t('candidates.email', 'Interviewer Email Address')}
          id="interviewerEmail"
          type="email"
          icon={Mail}
          placeholder="e.g. m.johnson@techcorp.com"
          error={errors.interviewerEmail}
          {...register('interviewerEmail', {
            required: t('common.error', 'Interviewer email is required'),
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

        {/* Conditional Field: Meeting Link (ONLINE) */}
        {watchInterviewType === 'ONLINE' && (
          <div className="md:col-span-2">
            <InputField
              label={t('interviews.meetingLink', 'Meeting Link')}
              id="meetingLink"
              icon={LinkIcon}
              placeholder="e.g. https://meet.google.com/abc-defg-hij"
              error={errors.meetingLink}
              {...register('meetingLink', {
                required: t('common.error', 'Meeting Link is required for online interviews'),
                maxLength: {
                  value: 500,
                  message: 'Meeting link must not exceed 500 characters',
                },
              })}
            />
          </div>
        )}

        {/* Conditional Field: Meeting Location (OFFLINE) */}
        {watchInterviewType === 'OFFLINE' && (
          <div className="md:col-span-2">
            <InputField
              label={t('interviews.location', 'Meeting Location')}
              id="meetingLocation"
              icon={MapPin}
              placeholder="e.g. TechCorp HQ, 3rd Floor Conference Room B"
              error={errors.meetingLocation}
              {...register('meetingLocation', {
                required: t('common.error', 'Meeting Location is required for offline interviews'),
                maxLength: {
                  value: 500,
                  message: 'Meeting location must not exceed 500 characters',
                },
              })}
            />
          </div>
        )}

        {/* Notes */}
        <div className="md:col-span-2 space-y-1.5">
          <label htmlFor="notes" className="block text-sm font-semibold text-gray-700">
            {t('interviews.notes', 'Internal Notes')}
          </label>
          <div className="relative rounded-md shadow-sm">
            <div className="absolute inset-y-0 left-0 pl-3 pt-3 flex items-start pointer-events-none">
              <AlignLeft className="h-5 w-5 text-gray-400" />
            </div>
            <textarea
              id="notes"
              rows={4}
              placeholder="Focus areas, system design topics, custom code instructions..."
              className={`block w-full pl-10 pr-3.5 py-2.5 text-sm rounded-xl border focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors ${
                errors.notes ? 'border-red-300 bg-red-50 text-red-900' : 'border-gray-300'
              }`}
              {...register('notes', {
                maxLength: {
                  value: 2000,
                  message: 'Notes must not exceed 2000 characters',
                },
              })}
            />
          </div>
          {errors.notes && (
            <p className="text-xs text-red-600 font-semibold mt-1">{errors.notes.message}</p>
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

export default InterviewForm;
