import React, { useState, useEffect } from 'react';
import { useForm, Controller } from 'react-hook-form';
import { useTranslation } from 'react-i18next';
import { AlignLeft } from 'lucide-react';
import axiosClient from '../../../services/axiosClient';
import SearchableSelect from './SearchableSelect';
import SubmitButton from '../../../components/ui/SubmitButton';

const ApplicationForm = ({ onSubmit, loading }) => {
  const { t } = useTranslation();
  const [candidates, setCandidates] = useState([]);
  const [jobs, setJobs] = useState([]);
  const [fetching, setFetching] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      setFetching(true);
      try {
        const [candRes, jobRes] = await Promise.all([
          axiosClient.get('/candidates', { params: { page: 0, size: 500, sortBy: 'fullName', sortDirection: 'asc' } }),
          axiosClient.get('/jobs', { params: { page: 0, size: 500, sortBy: 'title', sortDirection: 'asc' } })
        ]);

        const filteredCandidates = (candRes.data.data.content || [])
          .filter(c => c.hasCv)
          .map(c => ({
            value: c.id,
            label: c.fullName,
            sublabel: `${c.email} • ${c.yearsOfExperience || 0} yrs exp`
          }));

        const filteredJobs = (jobRes.data.data.content || [])
          .filter(j => j.status === 'OPEN')
          .map(j => ({
            value: j.id,
            label: j.title,
            sublabel: `${j.company?.companyName} • ${j.location || 'Remote'}`
          }));

        setCandidates(filteredCandidates);
        setJobs(filteredJobs);
      } catch (err) {
        console.error('Error fetching data for application form:', err);
      } finally {
        setFetching(false);
      }
    };
    fetchData();
  }, []);

  const {
    control,
    register,
    handleSubmit,
    formState: { errors },
  } = useForm({
    defaultValues: {
      candidateId: '',
      jobId: '',
      coverLetter: '',
    },
  });

  return (
    <form onSubmit={handleSubmit(onSubmit)} className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Candidate Selector */}
        <Controller
          name="candidateId"
          control={control}
          rules={{ required: t('common.error', 'Candidate is required') }}
          render={({ field }) => (
            <SearchableSelect
              label={t('applications.selectCandidate', 'Select Candidate')}
              placeholder={t('applications.selectCandidate', 'Search and select candidate...')}
              options={candidates}
              value={field.value}
              onChange={field.onChange}
              error={errors.candidateId}
              loading={fetching}
              noOptionsMessage={
                fetching 
                  ? t('common.loading', 'Loading candidates...') 
                  : t('common.noData', 'No candidates with uploaded resumes found')
              }
            />
          )}
        />

        {/* Job Selector */}
        <Controller
          name="jobId"
          control={control}
          rules={{ required: t('common.error', 'Job is required') }}
          render={({ field }) => (
            <SearchableSelect
              label={t('applications.selectJob', 'Select Job Opening')}
              placeholder={t('applications.selectJob', 'Search and select open job...')}
              options={jobs}
              value={field.value}
              onChange={field.onChange}
              error={errors.jobId}
              loading={fetching}
              noOptionsMessage={
                fetching 
                  ? t('common.loading', 'Loading jobs...') 
                  : t('common.noData', 'No open jobs found')
              }
            />
          )}
        />

        {/* Cover Letter */}
        <div className="md:col-span-2 space-y-1.5">
          <label htmlFor="coverLetter" className="block text-sm font-semibold text-gray-700">
            {t('applications.notes', 'Cover Letter')}
          </label>
          <div className="relative rounded-md shadow-sm">
            <div className="absolute inset-y-0 left-0 pl-3 pt-3 flex items-start pointer-events-none">
              <AlignLeft className="h-5 w-5 text-gray-400" />
            </div>
            <textarea
              id="coverLetter"
              rows={6}
              placeholder="Paste candidate cover letter here..."
              className="block w-full pl-10 pr-3.5 py-2.5 text-sm rounded-xl border border-gray-300 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
              {...register('coverLetter')}
            />
          </div>
        </div>
      </div>

      <div className="flex justify-end gap-3 pt-4 border-t border-gray-100">
        <SubmitButton loading={loading} className="w-auto px-6">
          {t('applications.addApplication', 'Submit Application')}
        </SubmitButton>
      </div>
    </form>
  );
};

export default ApplicationForm;
