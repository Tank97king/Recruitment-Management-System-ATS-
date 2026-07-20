import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { toast } from 'react-hot-toast';
import { ArrowLeft } from 'lucide-react';
import axiosClient from '../../services/axiosClient';
import InterviewForm from './components/InterviewForm';

const CreateInterview = () => {
  const navigate = useNavigate();
  const { t } = useTranslation();
  const [loading, setLoading] = useState(false);

  const onSubmit = async (data) => {
    setLoading(true);
    try {
      await axiosClient.post('/interviews', data);
      toast.success(t('common.success', 'Interview scheduled successfully!'));
      navigate('/interviews');
    } catch (err) {
      console.error('Error scheduling interview:', err);
      const msg = err.response?.data?.message || t('common.error', 'Failed to schedule interview.');
      toast.error(msg);
    } finally {
      setLoading(false);
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
          <h2 className="text-2xl font-bold text-gray-800 tracking-tight">{t('interviews.createInterview', 'Schedule Interview')}</h2>
          <p className="text-xs text-gray-400 font-semibold uppercase tracking-wider">{t('interviews.subtitle', 'Set up a new candidate evaluation session')}</p>
        </div>
      </div>

      <div className="bg-white border border-gray-200 rounded-2xl p-6 md:p-8 shadow-sm">
        <InterviewForm onSubmit={onSubmit} loading={loading} submitText={t('interviews.scheduleInterview', 'Schedule Interview')} />
      </div>
    </div>
  );
};

export default CreateInterview;
