export type RiskLevel = 'GREEN' | 'YELLOW' | 'RED';

export interface Policy {
  id: number;
  name: string;
  department: string;
  minRestHours: number;
  maxConsecutiveNights: number;
  maxWeeklyHours: number;
  churnThreshold: number;
}

export interface Violation {
  ruleCode: string;
  severity: string;
  message: string;
  evidenceJson: string;
}

export interface StaffRisk {
  staffId: number;
  staffName: string;
  totalPoints: number;
  riskLevel: RiskLevel;
  churnIndex: number;
  violations: Violation[];
}

export interface AnalysisSummary {
  greenCount: number;
  yellowCount: number;
  redCount: number;
}

export interface AnalysisReport {
  runId: number;
  weekStart: string;
  policyId: number;
  policyName: string;
  runType: string;
  summary: AnalysisSummary;
  staffRisks: StaffRisk[];
}

export interface RunAnalysisResponse {
  runId: number;
  weekStart: string;
  policyId: number;
  runType: string;
}

export interface PolicyDiffEntry {
  staffId: number;
  staffName: string;
  riskA: string;
  riskB: string;
  pointsDelta: number;
  violationCountDelta: number;
}

export interface PolicyDiffResponse {
  weekStart: string;
  policyIdA: number;
  policyIdB: number;
  additionalRedCount: number;
  diffs: PolicyDiffEntry[];
}

export interface WhatIfStaffDelta {
  staffId: number;
  staffName: string;
  riskBefore: string;
  riskAfter: string;
  violationsBefore: number;
  violationsAfter: number;
  changes: string[];
}

export interface WhatIfResponse {
  weekStart: string;
  policyId: number;
  deltas: WhatIfStaffDelta[];
}

export interface Shift {
  id: number;
  staffId: number;
  startAt: string;
  endAt: string;
  shiftType: string;
  revisionCount: number;
}

export interface Staff {
  id: number;
  name: string;
  role: string;
  department: string;
  sensitivityFactor: number;
}

export interface StaffPageResponse {
  items: Staff[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface ImportLineError {
  line: number;
  message: string;
}

export interface StaffImportResponse {
  created: number;
  updated: number;
  skipped: number;
  total: number;
  errors: ImportLineError[];
}

export interface ShiftImportResponse {
  created: number;
  updated: number;
  total: number;
  errors?: ImportLineError[];
}

export interface RosterImportResponse {
  weekStart: string;
  staffCreated: number;
  staffMatched: number;
  shiftsCreated: number;
  shiftsUpdated: number;
  shiftRows: number;
  errors: ImportLineError[];
}

export interface SuggestionImpact {
  targetRiskBefore: string;
  targetRiskAfter: string;
  targetPointsDelta: number;
  redCountBefore: number;
  redCountAfter: number;
}

export interface SuggestionItem {
  rank: number;
  action: string;
  shiftId: number;
  fromStaffId: number;
  fromStaffName: string;
  toStaffId: number;
  toStaffName: string;
  shiftSummary: string;
  reasonCodes: string[];
  impact: SuggestionImpact;
  explanation: string;
}

export interface SuggestionBaseline {
  redCount: number;
  yellowCount: number;
  greenCount: number;
}

export interface RosterSuggestionResponse {
  weekStart: string;
  policyId: number;
  baseline: SuggestionBaseline;
  suggestions: SuggestionItem[];
  warning?: string;
}

export type SlotKind = 'MORNING' | 'AFTERNOON' | 'NIGHT';

export interface BoardShiftCard {
  shiftId: number;
  staffId: number;
  staffName: string;
  department: string;
  role: string;
  startAt: string;
  endAt: string;
  shiftType: string;
  slot: SlotKind;
  revisionCount: number;
  warnings: string[];
}

export interface WeekBoardDay {
  date: string;
  label: string;
  cells: Record<SlotKind, BoardShiftCard[]>;
}

export interface WeekBoardSummary {
  totalShifts: number;
  staffCount: number;
}

export interface WeekBoard {
  weekStart: string;
  weekEnd: string;
  slots: SlotKind[];
  days: WeekBoardDay[];
  summary: WeekBoardSummary;
}

export type WizardStep = 1 | 2 | 3;
