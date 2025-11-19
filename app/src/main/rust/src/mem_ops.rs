use jni::JNIEnv;
use jni::objects::{JByteArray, JObject};
use jni::sys::{jint, jlong};
use jni_macro::jni_method;
use log::debug;
use std::alloc::{Layout, alloc, dealloc};

/// Allocate memory of specified size (like malloc)
#[jni_method(70, "moe/fuqiuluo/mamu/driver/MemoryOps", "nativeAlloc", "(I)J")]
pub fn jni_practice_alloc(_env: JNIEnv, _obj: JObject, size: jint) -> jlong {
    if size <= 0 {
        return 0;
    }

    let layout = Layout::from_size_align(size as usize, 8).unwrap();
    let ptr = unsafe { alloc(layout) };

    if ptr.is_null() {
        return 0;
    }

    let address = ptr as jlong;
    debug!("Practice memory allocated: address=0x{:x}, size={}", address, size);

    address
}

/// Free allocated memory
#[jni_method(70, "moe/fuqiuluo/mamu/driver/MemoryOps", "nativeFree", "(JI)V")]
pub fn jni_practice_free(_env: JNIEnv, _obj: JObject, address: jlong, size: jint) {
    if address == 0 || size <= 0 {
        return;
    }

    let ptr = address as *mut u8;
    let layout = Layout::from_size_align(size as usize, 8).unwrap();

    unsafe {
        dealloc(ptr, layout);
    }

    debug!("Practice memory freed: address=0x{:x}, size={}", address, size);
}

/// Read bytes from memory
#[jni_method(70, "moe/fuqiuluo/mamu/driver/MemoryOps", "nativeRead", "(JI)[B")]
pub fn jni_practice_read<'l>(mut env: JNIEnv<'l>, _obj: JObject, address: jlong, size: jint) -> JByteArray<'l> {
    let ptr = address as *const u8;
    let bytes = unsafe { std::slice::from_raw_parts(ptr, size as usize) };

    let result = env.new_byte_array(size).unwrap();
    env.set_byte_array_region(&result, 0, bytemuck::cast_slice(bytes)).unwrap();

    debug!("Practice memory read: address=0x{:x}, size={}", address, size);

    result
}

/// Write bytes to memory
#[jni_method(70, "moe/fuqiuluo/mamu/driver/MemoryOps", "nativeWrite", "(J[B)V")]
pub fn jni_practice_write(mut env: JNIEnv, _obj: JObject, address: jlong, data: JByteArray) {
    let len = env.get_array_length(&data).unwrap() as usize;
    let ptr = address as *mut u8;

    let mut buffer = vec![0i8; len];
    env.get_byte_array_region(&data, 0, &mut buffer).unwrap();

    unsafe {
        std::ptr::copy_nonoverlapping(buffer.as_ptr() as *const u8, ptr, len);
    }

    debug!("Practice memory write: address=0x{:x}, size={}", address, len);
}

/// Get current process ID
#[jni_method(70, "moe/fuqiuluo/mamu/driver/MemoryOps", "nativeGetPid", "()I")]
pub fn jni_practice_get_pid(_env: JNIEnv, _obj: JObject) -> jint {
    std::process::id() as jint
}