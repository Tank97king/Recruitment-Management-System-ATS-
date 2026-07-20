import React, { Suspense, lazy } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { Loader2 } from 'lucide-react';
import ProtectedRoute from '../components/common/ProtectedRoute';
import Layout from '../components/layout/Layout';
import ErrorBoundary from '../components/common/ErrorBoundary';

// Lazy-loaded Pages — each page chunk is only fetched when navigated to
const Login          = lazy(() => import('../pages/auth/Login'));
const Dashboard      = lazy(() => import('../pages/dashboard/Dashboard'));
const Companies      = lazy(() => import('../pages/company/Companies'));
const CreateCompany  = lazy(() => import('../pages/company/CreateCompany'));
const EditCompany    = lazy(() => import('../pages/company/EditCompany'));
const CompanyDetail  = lazy(() => import('../pages/company/CompanyDetail'));
const Jobs           = lazy(() => import('../pages/jobs/Jobs'));
const CreateJob      = lazy(() => import('../pages/jobs/CreateJob'));
const EditJob        = lazy(() => import('../pages/jobs/EditJob'));
const JobDetail      = lazy(() => import('../pages/jobs/JobDetail'));
const Candidates     = lazy(() => import('../pages/candidates/Candidates'));
const CreateCandidate = lazy(() => import('../pages/candidates/CreateCandidate'));
const EditCandidate  = lazy(() => import('../pages/candidates/EditCandidate'));
const CandidateDetail = lazy(() => import('../pages/candidates/CandidateDetail'));
const Applications   = lazy(() => import('../pages/applications/Applications'));
const CreateApplication = lazy(() => import('../pages/applications/CreateApplication'));
const ApplicationDetail = lazy(() => import('../pages/applications/ApplicationDetail'));
const Interviews     = lazy(() => import('../pages/interviews/Interviews'));
const CreateInterview = lazy(() => import('../pages/interviews/CreateInterview'));
const EditInterview  = lazy(() => import('../pages/interviews/EditInterview'));
const InterviewDetail = lazy(() => import('../pages/interviews/InterviewDetail'));
const Pipeline       = lazy(() => import('../pages/pipeline/Pipeline'));
const Profile        = lazy(() => import('../pages/Profile'));
const NotFound       = lazy(() => import('../pages/NotFound'));

// Shared page-level loading spinner
const PageLoader = () => (
  <div className="flex flex-col items-center justify-center min-h-[60vh] gap-3 text-gray-400">
    <Loader2 className="w-8 h-8 animate-spin text-blue-500" />
    <span className="text-sm font-semibold">Loading...</span>
  </div>
);

const AppRoutes = () => {
  return (
    <ErrorBoundary>
      <Suspense fallback={<PageLoader />}>
        <Routes>
          {/* Public Routes */}
          <Route path="/login" element={<Login />} />

          {/* Protected Layout Routes */}
          <Route
            path="/"
            element={
              <ProtectedRoute>
                <Layout />
              </ProtectedRoute>
            }
          >
            <Route index element={<Navigate to="/dashboard" replace />} />
            <Route path="dashboard" element={<Dashboard />} />

            {/* Company Routes */}
            <Route path="companies" element={<Companies />} />
            <Route path="companies/new" element={<CreateCompany />} />
            <Route path="companies/:id" element={<CompanyDetail />} />
            <Route path="companies/:id/edit" element={<EditCompany />} />

            {/* Job Routes */}
            <Route path="jobs" element={<Jobs />} />
            <Route path="jobs/new" element={<CreateJob />} />
            <Route path="jobs/:id" element={<JobDetail />} />
            <Route path="jobs/:id/edit" element={<EditJob />} />

            {/* Candidate Routes */}
            <Route path="candidates" element={<Candidates />} />
            <Route path="candidates/new" element={<CreateCandidate />} />
            <Route path="candidates/:id" element={<CandidateDetail />} />
            <Route path="candidates/:id/edit" element={<EditCandidate />} />

            {/* Application Routes */}
            <Route path="applications" element={<Applications />} />
            <Route path="applications/new" element={<CreateApplication />} />
            <Route path="applications/:id" element={<ApplicationDetail />} />

            {/* Interview Routes */}
            <Route path="interviews" element={<Interviews />} />
            <Route path="interviews/new" element={<CreateInterview />} />
            <Route path="interviews/:id" element={<InterviewDetail />} />
            <Route path="interviews/:id/edit" element={<EditInterview />} />

            {/* Pipeline & Profile Routes */}
            <Route path="pipeline" element={<Pipeline />} />
            <Route path="profile" element={<Profile />} />
          </Route>

          {/* Wildcard 404 handler */}
          <Route path="*" element={<NotFound />} />
        </Routes>
      </Suspense>
    </ErrorBoundary>
  );
};

export default AppRoutes;
