import { useEffect, useState } from 'react';
import { api } from './api/client';
import { AnalysisPage } from './pages/AnalysisPage';
import { ScheduleBoardPage } from './pages/ScheduleBoardPage';
import { StaffPage } from './pages/StaffPage';
import type { WizardStep } from './types';
import { toMonday } from './utils/week';

export default function App() {
  const [step, setStep] = useState<WizardStep>(1);
  const [weekStart, setWeekStart] = useState(toMonday());
  const [policyId, setPolicyId] = useState(1);
  const [staffCount, setStaffCount] = useState(0);
  const [shiftCount, setShiftCount] = useState(0);
  const [connectionError, setConnectionError] = useState(false);

  useEffect(() => {
    api.health()
      .then(() => setConnectionError(false))
      .catch(() => setConnectionError(true));
    api.staff({ size: 1 })
      .then((p) => setStaffCount(p.totalElements))
      .catch(() => setStaffCount(0));
  }, []);

  const canAnalyze = shiftCount > 0;

  const goStep = (s: WizardStep) => {
    if (s === 3 && !canAnalyze) return;
    setStep(s);
  };

  return (
    <div className="layout">
      <h1>RotaGuard</h1>
      <p className="subtitle">Lịch trực và đánh giá mệt mỏi</p>

      {connectionError && (
        <div className="error">Không kết nối được máy chủ. Kiểm tra dịch vụ backend.</div>
      )}

      <nav className="step-nav">
        <button
          type="button"
          className={step === 1 ? 'active' : ''}
          onClick={() => goStep(1)}
        >
          1. Nhân sự{staffCount > 0 ? ` (${staffCount})` : ''}
        </button>
        <button type="button" className={step === 2 ? 'active' : ''} onClick={() => goStep(2)}>
          2. Lịch tuần{shiftCount > 0 ? ` (${shiftCount} ca)` : ''}
        </button>
        <button
          type="button"
          className={step === 3 ? 'active' : ''}
          onClick={() => goStep(3)}
          disabled={!canAnalyze}
        >
          3. Phân tích
        </button>
      </nav>

      {step === 1 && (
        <StaffPage onNext={() => setStep(2)} onStaffChange={setStaffCount} />
      )}
      {step === 2 && (
        <ScheduleBoardPage
          weekStart={weekStart}
          onWeekStartChange={setWeekStart}
          onNext={() => setStep(3)}
          onShiftCountChange={setShiftCount}
          onStaffCountChange={setStaffCount}
        />
      )}
      {step === 3 && (
        <AnalysisPage
          weekStart={weekStart}
          policyId={policyId}
          onPolicyIdChange={setPolicyId}
          canAnalyze={canAnalyze}
        />
      )}
    </div>
  );
}
