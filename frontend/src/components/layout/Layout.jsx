import React, { useState } from 'react';
import { Outlet, NavLink, useNavigate, useLocation } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useAuth } from '../../context/AuthContext';
import LanguageSwitcher from '../common/LanguageSwitcher';
import { 
  LayoutDashboard, 
  Building2, 
  Briefcase, 
  Users, 
  FileText, 
  Calendar, 
  User, 
  LogOut, 
  Menu, 
  X,
  Bell,
  LayoutGrid
} from 'lucide-react';

const Layout = () => {
  const { t } = useTranslation();
  const { currentUser, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const navigation = [
    { nameKey: 'dashboard', defaultName: 'Dashboard', href: '/dashboard', icon: LayoutDashboard },
    { nameKey: 'companies', defaultName: 'Companies', href: '/companies', icon: Building2 },
    { nameKey: 'jobs', defaultName: 'Jobs', href: '/jobs', icon: Briefcase },
    { nameKey: 'candidates', defaultName: 'Candidates', href: '/candidates', icon: Users },
    { nameKey: 'applications', defaultName: 'Applications', href: '/applications', icon: FileText },
    { nameKey: 'pipeline', defaultName: 'Pipeline', href: '/pipeline', icon: LayoutGrid },
    { nameKey: 'interviews', defaultName: 'Interviews', href: '/interviews', icon: Calendar },
    { nameKey: 'profile', defaultName: 'Profile', href: '/profile', icon: User },
  ];

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const getPageTitle = () => {
    const activeRoute = navigation.find(item => location.pathname.startsWith(item.href));
    return activeRoute ? t(`nav.${activeRoute.nameKey}`, activeRoute.defaultName) : t('nav.system', 'System');
  };

  return (
    <div className="min-h-screen bg-gray-50 flex">
      {/* Static sidebar for desktop */}
      <aside className="hidden md:flex md:w-64 md:flex-col md:fixed md:inset-y-0 bg-white border-r border-gray-200">
        <div className="flex-1 flex flex-col min-h-0">
          <div className="flex items-center h-16 flex-shrink-0 px-6 border-b border-gray-200">
            <div className="flex items-center gap-2.5">
              <div className="w-9 h-9 rounded-lg bg-blue-600 flex items-center justify-center shadow-md shadow-blue-200 animate-pulse-slow">
                <Briefcase className="w-5 h-5 text-white" />
              </div>
              <span className="font-bold text-xl tracking-tight bg-gradient-to-r from-blue-600 to-blue-800 bg-clip-text text-transparent">
                {t('nav.portalTitle', 'ATS Portal')}
              </span>
            </div>
          </div>
          <nav className="flex-1 px-4 py-6 space-y-1 overflow-y-auto">
            {navigation.map((item) => {
              const Icon = item.icon;
              const title = t(`nav.${item.nameKey}`, item.defaultName);
              return (
                <NavLink
                  key={item.nameKey}
                  to={item.href}
                  aria-current={location.pathname.startsWith(item.href) ? 'page' : undefined}
                  className={({ isActive }) =>
                    `flex items-center px-4 py-3 text-sm font-medium rounded-xl transition-all duration-100 group gap-3 ${
                      isActive
                        ? 'bg-blue-50 text-blue-700 font-semibold'
                        : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
                    }`
                  }
                >
                  <Icon className="w-5 h-5 flex-shrink-0" />
                  {title}
                </NavLink>
              );
            })}
          </nav>
          <div className="p-4 border-t border-gray-200">
            <button
              onClick={handleLogout}
              className="flex w-full items-center px-4 py-3 text-sm font-medium text-red-600 rounded-xl hover:bg-red-50 hover:text-red-700 transition-all duration-100 gap-3"
            >
              <LogOut className="w-5 h-5 flex-shrink-0" />
              {t('nav.signOut', 'Sign Out')}
            </button>
          </div>
        </div>
      </aside>

      {/* Mobile menu container */}
      {mobileMenuOpen && (
        <div className="fixed inset-0 z-40 flex md:hidden">
          <div className="fixed inset-0 bg-gray-600 bg-opacity-75 transition-opacity" onClick={() => setMobileMenuOpen(false)}></div>
          <div className="relative flex-1 flex flex-col max-w-xs w-full bg-white transition-transform duration-300 transform">
            <div className="absolute top-0 right-0 -mr-12 pt-2">
              <button
                type="button"
                aria-label="Close navigation menu"
                className="ml-1 flex items-center justify-center h-10 w-10 rounded-full focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white"
                onClick={() => setMobileMenuOpen(false)}
              >
                <X className="h-6 w-6 text-white" />
              </button>
            </div>
            <div className="flex-1 h-0 pt-5 pb-4 overflow-y-auto">
              <div className="flex-shrink-0 flex items-center px-6 gap-2.5">
                <div className="w-9 h-9 rounded-lg bg-blue-600 flex items-center justify-center">
                  <Briefcase className="w-5 h-5 text-white" />
                </div>
                <span className="font-bold text-xl tracking-tight text-gray-900">{t('nav.portalTitle', 'ATS Portal')}</span>
              </div>
              <nav className="mt-8 px-4 space-y-1">
                {navigation.map((item) => {
                  const Icon = item.icon;
                  const title = t(`nav.${item.nameKey}`, item.defaultName);
                  return (
                    <NavLink
                      key={item.nameKey}
                      to={item.href}
                      onClick={() => setMobileMenuOpen(false)}
                      className={({ isActive }) =>
                        `flex items-center px-4 py-3 text-sm font-medium rounded-xl transition-all duration-100 gap-3 ${
                          isActive
                            ? 'bg-blue-50 text-blue-700 font-semibold'
                            : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
                        }`
                      }
                    >
                      <Icon className="w-5 h-5 flex-shrink-0" />
                      {title}
                    </NavLink>
                  );
                })}
              </nav>
            </div>
            <div className="flex-shrink-0 flex border-t border-gray-200 p-4">
              <button
                onClick={handleLogout}
                className="flex w-full items-center px-4 py-3 text-sm font-medium text-red-600 rounded-xl hover:bg-red-50 hover:text-red-700 transition-all duration-100 gap-3"
              >
                <LogOut className="w-5 h-5 flex-shrink-0" />
                {t('nav.signOut', 'Sign Out')}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Main layout container */}
      <div className="md:pl-64 flex flex-col flex-1 w-full">
        {/* Top Navbar */}
        <header className="sticky top-0 z-10 flex-shrink-0 flex h-16 bg-white border-b border-gray-200">
          <button
            type="button"
            aria-label="Open navigation menu"
            aria-expanded={mobileMenuOpen}
            aria-controls="mobile-nav"
            className="px-4 border-r border-gray-200 text-gray-500 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-blue-500 md:hidden hover:text-gray-700"
            onClick={() => setMobileMenuOpen(true)}
          >
            <Menu className="h-6 w-6" />
          </button>
          
          <div className="flex-1 px-4 sm:px-6 md:px-8 flex justify-between items-center">
            <h1 className="text-xl font-bold text-gray-800 tracking-tight">{getPageTitle()}</h1>
            <div className="flex items-center gap-4">
              <LanguageSwitcher />

              <button
                aria-label="Notifications"
                className="p-1.5 rounded-lg text-gray-400 hover:text-gray-500 hover:bg-gray-50 transition-colors"
              >
                <Bell className="w-5 h-5" />
              </button>
              
              <div className="h-8 w-px bg-gray-200"></div>

              <div className="flex items-center gap-3">
                <div className="flex flex-col text-right hidden sm:flex">
                  <span className="text-sm font-semibold text-gray-800">{currentUser?.fullName || 'User'}</span>
                  <span className="text-xs text-gray-400 font-medium capitalize">{currentUser?.role?.toLowerCase() || 'Recruiter'}</span>
                </div>
                <div className="w-9 h-9 rounded-full bg-blue-100 flex items-center justify-center text-blue-700 font-semibold shadow-inner border border-blue-200">
                  {(currentUser?.fullName || 'U').charAt(0).toUpperCase()}
                </div>
              </div>
            </div>
          </div>
        </header>

        {/* Main page content */}
        <main className="flex-1 py-8 px-4 sm:px-6 md:px-8 overflow-y-auto">
          <Outlet />
        </main>
      </div>
    </div>
  );
};

export default Layout;
