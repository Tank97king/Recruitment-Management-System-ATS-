import React, { useState, useEffect } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { toast } from 'react-hot-toast';
import { ArrowLeft, Loader2 } from 'lucide-react';
import axiosClient from '../../services/axiosClient';
import InterviewForm from './components/InterviewForm';

const EditInterview = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { t } = useTranslation();
  const [loading, setLoading] = useState(true);
  const [submitLoading, setSubmitLoading] = useState(false);
  const [interview, setInterview] = useState(null);

  useEffect(() => {
    const fetchInterview = async () => {
      try {
        const response = await axiosClient.get(`/interviews/${id}`);
        setInterview(response.data.data);
      } catch (err) {
        console.error('Error fetching interview profile:', err);
        toast.error(t('common.error', 'Failed to load interview details.'));
        navigate('/interviews');
      } finally {
        setLoading(false);
      }
    };
    fetchInterview();
  }, [id, navigate, t]);

  const onSubmit = async (data) => {
    setSubmitLoading(true);
    const { jobApplicationId, ...payload } = data;

    try {
      await axiosClient.put(`/interviews/${id}`, payload);
      toast.success(t('common.success', 'Interview scheduled parameters updated successfully!'));
      navigate('/interviews');
    } catch (err) {
      console.error('Error updating interview:', err);
      const msg = err.response?.data?.message || t('common.error', 'Failed to update interview session.');
      toast.error(msg);
    } finally {
      setSubmitLoading(false);
    }
  };

  return (
    <div className="max-w-3xl mx-auto space-y-6 animate-fadeIn">
      <div className="flex items-center gap-4">
        <Link 
          to="/interviews" 
          className="p-2 border border-gray-200 text-gray-500 hover:text-gray-800 rounded-xl hover:bg-gray-50 transition-colors bg-white shadow-sm"
        >
          <ArrowLeft className="w-4 h-4" />
        </Link>
        <div>
          <h2 className="text-2xl font-bold text-gray-800 tracking-tight">{t('interviews.editInterview', 'Edit Scheduled Interview')}</h2>
          <p className="text-xs text-gray-400 font-semibold uppercase tracking-wider">{t('interviews.subtitle', 'Modify session coordinates or interviewer details')}</p>
        </div>
      </div>

      <div className="bg-white border border-gray-200 rounded-2xl p-6 md:p-8 shadow-sm">
        {loading ? (
          <div className="flex flex-col items-center justify-center py-12 gap-3 text-gray-450 font-semibold text-sm">
            <Loader2 className="w-8 h-8 animate-spin text-blue-500" />
            <span>{t('common.loading', 'Loading interview details...')}</span>
          </div>
        ) : (
          <InterviewForm 
            defaultValues={interview} 
            onSubmit={onSubmit} 
            loading={submitLoading} 
            submitText={t('interviews.editInterview', 'Save Session Details')} 
            isEdit={true} 
          />
        )}
      </div>
    </div>
  );
};

export default EditInterview;
