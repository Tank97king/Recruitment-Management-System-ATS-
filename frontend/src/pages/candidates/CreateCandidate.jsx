import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { toast } from 'react-hot-toast';
import { ArrowLeft } from 'lucide-react';
import axiosClient from '../../services/axiosClient';
import CandidateForm from './components/CandidateForm';

const CreateCandidate = () => {
  const navigate = useNavigate();
  const { t } = useTranslation();
  const [loading, setLoading] = useState(false);

  const onSubmit = async (data) => {
    setLoading(true);
    try {
      await axiosClient.post('/candidates', data);
      toast.success(t('common.success', 'Candidate profile created successfully!'));
      navigate('/candidates');
    } catch (err) {
      console.error('Error creating candidate profile:', err);
      const msg = err.response?.data?.message || t('common.error', 'Failed to create candidate profile.');
      toast.error(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-3xl mx-auto space-y-6 animate-fadeIn">
      <div className="flex items-center gap-4">
        <Link 
          to="/candidates" 
          className="p-2 border border-gray-200 text-gray-500 hover:text-gray-800 rounded-xl hover:bg-gray-50 transition-colors bg-white shadow-sm"
        >
          <ArrowLeft className="w-4 h-4" />
        </Link>
        <div>
          <h2 className="text-2xl font-bold text-gray-800 tracking-tight">{t('candidates.createCandidate', 'Create Candidate')}</h2>
          <p className="text-xs text-gray-400 font-semibold uppercase tracking-wider">{t('candidates.subtitle', 'Register a new candidate profile')}</p>
        </div>
      </div>

      <div className="bg-white border border-gray-200 rounded-2xl p-6 md:p-8 shadow-sm">
        <CandidateForm onSubmit={onSubmit} loading={loading} submitText={t('candidates.createCandidate', 'Create Profile')} />
      </div>
    </div>
  );
};

export default CreateCandidate;
