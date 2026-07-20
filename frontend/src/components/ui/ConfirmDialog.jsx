import React, { useEffect } from 'react';
import { useTranslation } from 'react-i18next';
import { AlertTriangle, Loader2 } from 'lucide-react';

const ConfirmDialog = ({
  open,
  title,
  message,
  onConfirm,
  onCancel,
  confirmText,
  cancelText,
  loading = false,
}) => {
  const { t } = useTranslation();
  const resolvedConfirmText = confirmText || t('common.confirm', 'Confirm');
  const resolvedCancelText = cancelText || t('common.cancel', 'Cancel');

  // Close on Escape key
  useEffect(() => {
    if (!open) return;
    const handleKey = (e) => { if (e.key === 'Escape' && !loading) onCancel(); };
    document.addEventListener('keydown', handleKey);
    return () => document.removeEventListener('keydown', handleKey);
  }, [open, loading, onCancel]);

  if (!open) return null;

  return (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center p-4"
      role="dialog"
      aria-modal="true"
      aria-labelledby="confirm-dialog-title"
    >
      {/* Backdrop */}
      <div
        className="fixed inset-0 bg-gray-900/60 transition-opacity"
        onClick={!loading ? onCancel : undefined}
      />

      {/* Dialog Body */}
      <div className="relative bg-white border border-gray-100 rounded-2xl max-w-md w-full p-6 shadow-xl space-y-5 animate-fadeIn">
        <div className="flex items-start gap-4">
          <div className="w-10 h-10 rounded-xl bg-red-50 text-red-600 border border-red-100 flex items-center justify-center flex-shrink-0">
            <AlertTriangle className="w-5 h-5" />
          </div>
          <div className="space-y-1">
            <h3 id="confirm-dialog-title" className="text-base font-bold text-gray-800">{title}</h3>
            <p className="text-sm text-gray-500 leading-relaxed font-medium">{message}</p>
          </div>
        </div>

        <div className="flex justify-end gap-3 pt-2">
          <button
            type="button"
            onClick={onCancel}
            disabled={loading}
            className="px-4 py-2.5 border border-gray-200 text-gray-600 hover:text-gray-800 font-bold rounded-xl hover:bg-gray-50 disabled:opacity-50 text-sm bg-white shadow-sm transition-all"
          >
            {resolvedCancelText}
          </button>
          <button
            type="button"
            onClick={onConfirm}
            disabled={loading}
            className="px-4 py-2.5 bg-red-600 hover:bg-red-700 text-white font-bold rounded-xl shadow-md shadow-red-100 text-sm disabled:opacity-50 transition-all flex items-center gap-2"
          >
            {loading ? (
              <>
                <Loader2 className="w-4 h-4 animate-spin" />
                <span>{t('common.loading', 'Loading...')}</span>
              </>
            ) : (
              resolvedConfirmText
            )}
          </button>
        </div>
      </div>
    </div>
  );
};

export default ConfirmDialog;
