-- Fix for existing team members to have ACTIVE status
-- This is necessary because TeamMemberStatus enum was introduced recently
-- and existing rows might have NULL status.

UPDATE team_members SET status = 'ACTIVE' WHERE status IS NULL;
