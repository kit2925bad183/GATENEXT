import React, { useState } from 'react';
import { usePwa } from '@/lib/pwaContext';
import { Modal } from './Modal';
import { Smartphone, Laptop, Apple, Share2, PlusSquare, Download, CheckCircle2 } from 'lucide-react';

export const PwaInstallGuideModal: React.FC = () => {
  const { showInstallGuide, closeInstallGuide, isInstalled } = usePwa();
  const [activeTab, setActiveTab] = useState<'ios' | 'android' | 'desktop'>('ios');

  if (!showInstallGuide) return null;

  return (
    <Modal
      isOpen={showInstallGuide}
      onClose={closeInstallGuide}
      title="Install GateNest App"
      maxWidth="md"
    >
      <div className="space-y-4">
        {isInstalled ? (
          <div className="p-4 bg-emerald-50 border border-emerald-200 rounded-xl text-center space-y-2">
            <CheckCircle2 className="w-8 h-8 text-emerald-600 mx-auto" />
            <p className="text-sm font-bold text-emerald-900">GateNest is already installed!</p>
            <p className="text-xs text-emerald-700">
              You are running GateNest in standalone app mode on this device.
            </p>
          </div>
        ) : (
          <>
            <p className="text-xs text-text-secondary leading-relaxed">
              Install GateNest to your home screen or desktop for fast gate pass generation, instant visitor notifications, and offline app shell caching.
            </p>

            {/* Platform Selector Tabs */}
            <div className="flex rounded-lg bg-surface p-1 border border-border">
              <button
                onClick={() => setActiveTab('ios')}
                className={`flex-1 py-1.5 text-xs font-semibold rounded-md flex items-center justify-center gap-1.5 transition-all ${
                  activeTab === 'ios'
                    ? 'bg-white text-primary shadow-sm'
                    : 'text-text-secondary hover:text-text-main'
                }`}
              >
                <Apple className="w-3.5 h-3.5" />
                iPhone / iPad
              </button>
              <button
                onClick={() => setActiveTab('android')}
                className={`flex-1 py-1.5 text-xs font-semibold rounded-md flex items-center justify-center gap-1.5 transition-all ${
                  activeTab === 'android'
                    ? 'bg-white text-primary shadow-sm'
                    : 'text-text-secondary hover:text-text-main'
                }`}
              >
                <Smartphone className="w-3.5 h-3.5" />
                Android / Chrome
              </button>
              <button
                onClick={() => setActiveTab('desktop')}
                className={`flex-1 py-1.5 text-xs font-semibold rounded-md flex items-center justify-center gap-1.5 transition-all ${
                  activeTab === 'desktop'
                    ? 'bg-white text-primary shadow-sm'
                    : 'text-text-secondary hover:text-text-main'
                }`}
              >
                <Laptop className="w-3.5 h-3.5" />
                Mac / Windows
              </button>
            </div>

            {/* Step-by-Step Instructions */}
            <div className="bg-surface/50 border border-border rounded-xl p-4 space-y-3">
              {activeTab === 'ios' && (
                <div className="space-y-2.5 text-xs text-text-main">
                  <div className="flex items-start gap-2.5">
                    <span className="w-5 h-5 rounded-full bg-primary/10 text-primary font-bold flex items-center justify-center flex-shrink-0 text-[11px]">
                      1
                    </span>
                    <p>
                      Open <strong>GateNest</strong> in Safari on your iPhone or iPad.
                    </p>
                  </div>
                  <div className="flex items-start gap-2.5">
                    <span className="w-5 h-5 rounded-full bg-primary/10 text-primary font-bold flex items-center justify-center flex-shrink-0 text-[11px]">
                      2
                    </span>
                    <p className="flex items-center gap-1 flex-wrap">
                      Tap the <Share2 className="w-3.5 h-3.5 text-primary inline" /> <strong>Share</strong> button at the bottom navigation bar.
                    </p>
                  </div>
                  <div className="flex items-start gap-2.5">
                    <span className="w-5 h-5 rounded-full bg-primary/10 text-primary font-bold flex items-center justify-center flex-shrink-0 text-[11px]">
                      3
                    </span>
                    <p className="flex items-center gap-1 flex-wrap">
                      Scroll down and select <PlusSquare className="w-3.5 h-3.5 text-primary inline" /> <strong>Add to Home Screen</strong>.
                    </p>
                  </div>
                  <div className="flex items-start gap-2.5">
                    <span className="w-5 h-5 rounded-full bg-primary/10 text-primary font-bold flex items-center justify-center flex-shrink-0 text-[11px]">
                      4
                    </span>
                    <p>
                      Tap <strong>Add</strong> in the top-right corner to finish.
                    </p>
                  </div>
                </div>
              )}

              {activeTab === 'android' && (
                <div className="space-y-2.5 text-xs text-text-main">
                  <div className="flex items-start gap-2.5">
                    <span className="w-5 h-5 rounded-full bg-primary/10 text-primary font-bold flex items-center justify-center flex-shrink-0 text-[11px]">
                      1
                    </span>
                    <p>
                      In Chrome, tap the <strong>three dots menu (⋮)</strong> in the top right.
                    </p>
                  </div>
                  <div className="flex items-start gap-2.5">
                    <span className="w-5 h-5 rounded-full bg-primary/10 text-primary font-bold flex items-center justify-center flex-shrink-0 text-[11px]">
                      2
                    </span>
                    <p className="flex items-center gap-1 flex-wrap">
                      Select <Download className="w-3.5 h-3.5 text-primary inline" /> <strong>Install app</strong> or <strong>Add to Home Screen</strong>.
                    </p>
                  </div>
                  <div className="flex items-start gap-2.5">
                    <span className="w-5 h-5 rounded-full bg-primary/10 text-primary font-bold flex items-center justify-center flex-shrink-0 text-[11px]">
                      3
                    </span>
                    <p>Confirm by tapping <strong>Install</strong>.</p>
                  </div>
                </div>
              )}

              {activeTab === 'desktop' && (
                <div className="space-y-2.5 text-xs text-text-main">
                  <div className="flex items-start gap-2.5">
                    <span className="w-5 h-5 rounded-full bg-primary/10 text-primary font-bold flex items-center justify-center flex-shrink-0 text-[11px]">
                      1
                    </span>
                    <p>
                      Look for the <Download className="w-3.5 h-3.5 text-primary inline" /> <strong>Install</strong> icon in your browser's address bar (Chrome or Edge).
                    </p>
                  </div>
                  <div className="flex items-start gap-2.5">
                    <span className="w-5 h-5 rounded-full bg-primary/10 text-primary font-bold flex items-center justify-center flex-shrink-0 text-[11px]">
                      2
                    </span>
                    <p>
                      Click <strong>Install GateNest</strong> when prompted.
                    </p>
                  </div>
                </div>
              )}
            </div>
          </>
        )}

        <div className="flex justify-end pt-2">
          <button
            onClick={closeInstallGuide}
            className="px-4 py-2 bg-surface text-text-main hover:bg-border rounded-lg text-xs font-semibold transition-colors"
          >
            Got it
          </button>
        </div>
      </div>
    </Modal>
  );
};
