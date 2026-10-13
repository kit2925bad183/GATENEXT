import React, { createContext, useContext, useState, useEffect } from 'react';
import { en } from './en';
import { ta } from './ta';
import { Language } from '../types';

export type TranslationKey = keyof typeof en | string;

interface I18nContextType {
  language: Language;
  setLanguage: (lang: Language) => void;
  t: (key: TranslationKey, params?: Record<string, string | number>) => string;
}

const dictionaries: Record<string, Record<string, string>> = {
  en: en as unknown as Record<string, string>,
  ta: ta as unknown as Record<string, string>,
};

const I18nContext = createContext<I18nContextType | undefined>(undefined);

export const I18nProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [language, setLanguageState] = useState<Language>(() => {
    const saved = localStorage.getItem('gatenest_lang') as Language;
    return saved === 'ta' ? 'ta' : 'en';
  });

  const setLanguage = (lang: Language) => {
    setLanguageState(lang);
    localStorage.setItem('gatenest_lang', lang);
  };

  useEffect(() => {
    document.documentElement.lang = language;
  }, [language]);

  const t = (key: TranslationKey, params?: Record<string, string | number>): string => {
    const dict = dictionaries[language] || dictionaries.en;
    let translation = dict[key] || dictionaries.en[key] || (key as string);

    if (params) {
      Object.entries(params).forEach(([paramKey, value]) => {
        translation = translation.replace(new RegExp(`{${paramKey}}`, 'g'), String(value));
      });
    }

    return translation;
  };

  return (
    <I18nContext.Provider value={{ language, setLanguage, t }}>
      {children}
    </I18nContext.Provider>
  );
};

export const useI18n = () => {
  const context = useContext(I18nContext);
  if (!context) {
    throw new Error('useI18n must be used within an I18nProvider');
  }
  return context;
};
