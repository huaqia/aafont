#!/usr/bin/python
# -*- coding: UTF-8 -*-
import os, shutil
import upyun

png_limit_size = 500 * 1024
jpg_limit_size = 300 * 1024

upyun_folder = 'upyun_tmp'
if os.path.exists(upyun_folder):
    shutil.rmtree(upyun_folder)
os.mkdir(upyun_folder)

upyun_handle = upyun.UpYun('bmob-cdn-28693', '5041566', '0qeskFAMl54lafhntq4iQCVGTemEeyXr')

def compress_pics(path):
    item_list = upyun_handle.getlist(path)
    for item in item_list:
        name = item['name']
        if item['type'] == 'N':
            if name.endswith('.png') or name.endswith('.jpg') or name.endswith('.jpeg'):
                size = int(item['size'])
                if name.endswith('.png'):
                    if size < png_limit_size:
                        continue
                else:
                    if size < jpg_limit_size:
                        continue
                save_path = os.path.join(upyun_folder, name)
                server_path = path + name
                f_out = open(save_path, 'wb')
                upyun_handle.get(server_path, f_out)
                f_out.close()
                if name.endswith('.png'):
                    os.popen('./pngquant --ext .png --force 256 --speed 1 --quality=50-60 ' + save_path)
                else:
                    os.system('./jpegoptim -m75 ' + save_path)
                try:
                    f_in = open(save_path, 'rb')
                    upyun_handle.put(server_path, f_in)
                    f_in.close()
                except:
                    print sub_path, file_name, '上传失败，请检查！'
                os.remove(save_path)
        elif item['type'] == 'F':
            compress_pics(path + name + '/')

compress_pics('/')
