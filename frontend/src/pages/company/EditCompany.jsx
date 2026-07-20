import React, { useState, useEffect } from 'react';
import { useNavigate, useParams, Link } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { toast } from 'react-hot-toast';
import { ArrowLeft, Loader2 } from 'lucide-react';
import axiosClient from '../../services/axiosClient';
import CompanyForm from './components/CompanyForm';

const EditCompany = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const { t } = useTranslation();
  const [loading, setLoading] = useState(true);
  const [submitLoading, setSubmitLoading] = useState(false);
  const [company, setCompany] = useState(null);

  useEffect(() => {
    const fetchCompany = async () => {
      try {
        const response = await axiosClient.get(`/companies/${id}`);
        setCompany(response.data.data);
      } catch (err) {
        console.error('Error fetching company details:', err);
        toast.error(t('common.error', 'Failed to load company profile.'));
        navigate('/companies');
      } finally {
        setLoading(false);
      }
    };
    fetchCompany();
  }, [id, navigate, t]);

  const onSubmit = async (data) => {
    setSubmitLoading(true);
    try {
      await axiosClient.put(`/companies/${id}`, data);
      toast.success(t('common.success', 'Company updated successfully!'));
      navigate('/companies');
    } catch (err) {
      console.error('Error updating company:', err);
      const msg = err.response?.data?.message || t('common.error', 'Failed to update company.');
      toast.error(msg);
    } finally {
      setSubmitLoading(false);
    }
  };

  return (
    <div className="max-w-3xl mx-auto space-y-6 animate-fadeIn">
      <div className="flex items-center gap-4">
        <Link 
          to="/companies" 
          className="p-2 border border-gray-200 text-gray-500 hover:text-gray-800 rounded-xl hover:bg-gray-50 transition-colors bg-white shadow-sm"
        >
          <ArrowLeft className="w-4 h-4" />
        </Link>
        <div>
          <h2 className="text-2xl font-bold text-gray-800 tracking-tight">{t('companies.editCompany', 'Edit Company')}</h2>
          <p className="text-xs text-gray-400 font-semibold uppercase tracking-wider">{t('companies.subtitle', 'Modify organization profiles')}</p>
        </div>
      </div>

      <div className="bg-white border border-gray-200 rounded-2xl p-6 md:p-8 shadow-sm">
        {loading ? (
          <div className="flex flex-col items-center justify-center py-12 gap-3 text-gray-450 font-semibold text-sm">
            <Loader2 className="w-8 h-8 animate-spin text-blue-500" />
            <span>{t('common.loading', 'Loading company details...')}</span>
          </div>
        ) : (
          <CompanyForm 
            defaultValues={company} 
            onSubmit={onSubmit} 
            loading={submitLoading} 
            submitText={t('companies.editCompany', 'Update Company')} 
          />
        )}
      </div>
    </div>
  );
};

export default EditCompany;
