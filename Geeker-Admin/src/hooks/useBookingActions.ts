import { ElMessage, ElMessageBox } from 'element-plus';
import { BookingAPI } from '@/api/booking';

export function useBookingActions(refreshCallback: () => Promise<void>) {
  const handleConfirmBooking = async (bookingId: number) => {
    try {
      await ElMessageBox.confirm('确定要确认这个预约吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      });
      await BookingAPI.update({ id: bookingId, status: 1 });
      ElMessage.success('确认成功！');
      await refreshCallback();
    } catch (e: any) {
      if (e !== 'cancel') {
        ElMessage.error(e.message || '操作失败');
      }
    }
  };

  const handleCompleteBooking = async (bookingId: number, message: string = '确定要完成这个预约吗？') => {
    try {
      await ElMessageBox.confirm(message, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      });
      await BookingAPI.update({ id: bookingId, status: 2 });
      ElMessage.success('完成成功！');
      await refreshCallback();
    } catch (e: any) {
      if (e !== 'cancel') {
        ElMessage.error(e.message || '操作失败');
      }
    }
  };

  const handleCancelBooking = async (bookingId: number, message: string = '确定要取消这个预约吗？') => {
    try {
      await ElMessageBox.confirm(message, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      });
      await BookingAPI.update({ id: bookingId, status: 3 });
      ElMessage.success('取消成功！');
      await refreshCallback();
    } catch (e: any) {
      if (e !== 'cancel') {
        ElMessage.error(e.message || '操作失败');
      }
    }
  };

  return {
    handleConfirmBooking,
    handleCompleteBooking,
    handleCancelBooking
  };
}
