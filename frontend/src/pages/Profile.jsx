import React, { useState, useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { toast } from 'react-hot-toast';
import { Key, Shield, User, Loader2 } from 'lucide-react';
import axiosClient from '../services/axiosClient';
import ProfileForm from './profile/components/ProfileForm';
import AvatarCard from './profile/components/AvatarCard';

const Profile = () => {
  const { t } = useTranslation();
  const [loading, setLoading] = useState(true);
  const [profile, setProfile] = useState(null);
  const [updating, setUpdating] = useState(false);

  const fetchProfile = async () => {
    setLoading(true);
    try {
      const response = await axiosClient.get('/users/me');
      setProfile(response.data.data);
    } catch (err) {
      console.error('Error fetching user profile:', err);
      toast.error(t('common.error', 'Failed to load user profile details.'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchProfile();
  }, []);

  const handleUpdateProfile = async (data) => {
    setUpdating(true);
    try {
      const response = await axiosClient.put('/users/me', data);
      toast.success(t('profile.updateSuccess', 'Profile details updated successfully!'));
      setProfile(response.data.data);
    } catch (err) {
      console.error('Error updating user profile:', err);
      const msg = err.response?.data?.message || t('common.error', 'Failed to save profile changes.');
      toast.error(msg);
    } finally {
      setUpdating(false);
    }
  };

  const permissions = [
    { name: t('jobs.addJob', 'Manage Job Postings'), desc: t('jobs.subtitle', 'Create, update, and close job postings.'), active: true },
    { name: t('candidates.title', 'Screen Candidates'), desc: t('candidates.subtitle', 'Read resumes, leave scores, and attach feedback.'), active: true },
    { name: t('interviews.scheduleInterview', 'Schedule Interviews'), desc: t('interviews.subtitle', 'Integrate calendar, assign panels, and issue links.'), active: true },
    { name: t('nav.system', 'Admin Control Panels'), desc: t('nav.portalTitle', 'Configure system settings, users and roles.'), active: profile?.role === 'ADMIN' },
  ];

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[400px] gap-3 text-gray-400 font-semibold text-sm">
        <Loader2 className="w-8 h-8 animate-spin text-blue-500" />
        <span>{t('common.loading', 'Synchronizing profile parameters...')}</span>
      </div>
    );
  }

  if (!profile) return null;

  return (
    <div className="max-w-5xl mx-auto space-y-6 animate-fadeIn">
      {/* Header title */}
      <div>
        <h2 className="text-2xl font-bold text-gray-800 tracking-tight">{t('profile.title', 'User Profile')}</h2>
        <p className="text-gray-500 mt-1">{t('profile.subtitle', 'Configure your personal preferences and manage system settings.')}</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 items-start">
        {/* Avatar Sidebar card */}
        <div className="space-y-6">
          <AvatarCard user={profile} />

          {/* Permissions panel */}
          <div className="bg-white border border-gray-200 rounded-2xl p-6 shadow-sm space-y-4">
            <h3 className="font-bold text-gray-800 text-sm border-b border-gray-100 pb-3 uppercase tracking-wider flex items-center gap-2">
              <Shield className="w-4 h-4 text-blue-500" /> {t('auth.role', 'Active Permissions')}
            </h3>
            <div className="space-y-4">
              {permissions.map((perm) => (
                <div key={perm.name} className="flex gap-3">
                  <div className={`mt-1.5 w-1.5 h-1.5 rounded-full flex-shrink-0 ${
                    perm.active ? 'bg-blue-600' : 'bg-gray-200'
                  }`} />
                  <div>
                    <div className={`text-xs font-bold ${perm.active ? 'text-gray-700' : 'text-gray-400 line-through'}`}>
                      {perm.name}
                    </div>
                    <p className="text-[10px] text-gray-450 mt-0.5 leading-relaxed font-semibold">{perm.desc}</p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Configuration settings form */}
        <div className="md:col-span-2 space-y-6">
          <div className="bg-white border border-gray-200 rounded-2xl p-6 md:p-8 shadow-sm space-y-6">
            <h3 className="font-bold text-gray-800 text-sm border-b border-gray-100 pb-3 uppercase tracking-wider flex items-center gap-2">
              <User className="w-4 h-4 text-blue-500" /> {t('profile.personalInfo', 'Account Settings')}
            </h3>
            <ProfileForm user={profile} onSubmit={handleUpdateProfile} loading={updating} />
          </div>

          {/* Change Password Form (Disabled) */}
          <div className="bg-white border border-gray-200 rounded-2xl p-6 md:p-8 shadow-sm space-y-6 opacity-60">
            <h3 className="font-bold text-gray-800 text-sm border-b border-gray-100 pb-3 uppercase tracking-wider flex items-center gap-2">
              <Key className="w-4 h-4 text-amber-500" /> {t('profile.changePassword', 'Change Password')}
            </h3>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-bold text-gray-400 uppercase tracking-wider">{t('profile.currentPassword', 'Current Password')}</label>
                <input
                  type="password"
                  disabled
                  placeholder="••••••••"
                  className="mt-1.5 block w-full px-3.5 py-2 text-xs border border-gray-200 rounded-xl bg-gray-50 text-gray-400 select-none cursor-not-allowed"
                />
              </div>
              <div>
                <label className="block text-xs font-bold text-gray-400 uppercase tracking-wider">{t('profile.newPassword', 'New Password')}</label>
                <input
                  type="password"
                  disabled
                  placeholder="••••••••"
                  className="mt-1.5 block w-full px-3.5 py-2 text-xs border border-gray-200 rounded-xl bg-gray-50 text-gray-400 select-none cursor-not-allowed"
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Profile;
